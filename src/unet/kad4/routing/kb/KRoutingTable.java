package unet.kad4.routing.kb;

import unet.kad4.utils.hash.CRC32c;
import unet.kad4.routing.inter.RoutingTable;
import unet.kad4.utils.Node;
import unet.kad4.utils.UID;
import unet.kad4.utils.net.AddressUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static unet.kad4.utils.Node.*;

public class KRoutingTable extends RoutingTable {

    private KBucket[] kBuckets;
    private InetAddress consensusExternalAddress;

    private LinkedHashMap<InetAddress, InetAddress> originPairs  = new LinkedHashMap<>(64, 0.75f, true){
        @Override
        protected boolean removeEldestEntry(Map.Entry<InetAddress, InetAddress> eldest){
            return (size() > 64);
        }
    };

    public KRoutingTable(){
        //uid = new UID(new byte[]{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 });
        //use UPnP to attempt getting IP
        //If fails go to fall back method...
        //We need to somehow allow the refresh to derive our ID...
        try{
            consensusExternalAddress = Inet4Address.getLocalHost();
        }catch(UnknownHostException e){
            e.printStackTrace();
        }

        deriveUID();

        kBuckets = new KBucket[UID.ID_LENGTH*8];
        for(int i = 0; i < UID.ID_LENGTH*8; i++){
            kBuckets[i] = new KBucket();
        }
    }

    @Override
    public void updatePublicIPConsensus(InetAddress source, InetAddress addr){
        if(!AddressUtils.isGlobalUnicast(addr)){
            return;
        }

        synchronized(originPairs){
            originPairs.put(source, addr);
            //System.err.println("CONSENSUS UPDATE: "+originPairs.size()+"  "+source.getHostAddress()+"  "+addr.getHostAddress());
            if(originPairs.size() > 20 && !addr.equals(consensusExternalAddress)){
                List<InetAddress> k = new ArrayList<>(originPairs.values());
                short res = 0, count = 1;

                for(short i = 1; i < k.size(); i++){
                    count += (k.get(i).equals(k.get(res))) ? 1 : -1;

                    if(count == 0){
                        res = i;
                        count = 1;
                    }
                }

                //CHANGE - TO AUTO UPDATE UID BASED OFF OF IP CONSENSUS CHANGES
                if(!consensusExternalAddress.equals(k.get(res))){
                    consensusExternalAddress = k.get(res);
                    deriveUID();
                    restart();
                }

                //consensusExternalAddress = k.get(res);
            }
        }
    }

    @Override
    public InetAddress getConsensusExternalAddress(){
        return consensusExternalAddress;
    }

    @Override
    public void deriveUID(){
        byte[] ip = consensusExternalAddress.getAddress();
        byte[] mask = ip.length == 4 ? V4_MASK : V6_MASK;

        for(int i = 0; i < ip.length; i++){
            ip[i] &= mask[i];
        }

        Random random = new Random();
        int rand = random.nextInt() & 0xFF;
        int r = rand & 0x7;

        ip[0] |= r << 5;

        CRC32c c = new CRC32c();
        c.update(ip, 0, ip.length);
        int crc = (int) c.getValue();

        // idk about this stuff below
        byte[] bid = new byte[UID.ID_LENGTH];
        bid[0] = (byte) ((crc >> 24) & 0xFF);
        bid[1] = (byte) ((crc >> 16) & 0xFF);
        bid[2] = (byte) (((crc >> 8) & 0xF8) | (random.nextInt() & 0x7));

        for(int i = 3; i < 19; i++){
            bid[i] = (byte) (random.nextInt() & 0xFF);
        }

        bid[19] = (byte) (rand & 0xFF);
        uid = new UID(bid);
    }

    @Override
    public synchronized void insert(Node n){
        if(secureOnly && !n.hasSecureID()){
            return;
        }

        if(!uid.equals(n.getUID())){
            int id = getBucketUID(n.getUID());

            boolean containsIP = false;
            for(KBucket b : kBuckets){
                if(b.containsIP(n)){
                    containsIP = true;
                    break;
                }
            }

            boolean containsUID = kBuckets[id].containsUID(n);

            if(containsIP == containsUID){
                kBuckets[id].insert(n);
            }
        }
    }

    @Override
    public synchronized boolean hasQueried(Node node, long now){
        int id = getBucketUID(node.getUID());
        if(!kBuckets[id].containsUID(node)){
            return false;
        }
        return kBuckets[id].hasQueried(node, now);
    }

    @Override
    public synchronized int getBucketUID(UID k){
        return uid.getDistance(k);
    }

    @Override
    public String toString(){
        int s = 0, c = 0;
        for(KBucket b : kBuckets){
            s += b.size();
            c += b.csize();
        }
        return s+" - SIZE    "+c+"  - CACHE";
    }

    @Override
    public synchronized List<Node> getAllNodes(){
        List<Node> nodes = new ArrayList<>();
        for(KBucket kBucket : kBuckets){
            nodes.addAll(kBucket.getAllNodes());
        }
        return nodes;
    }

    @Override
    public synchronized List<Node> findClosest(UID k, int r){
        TreeSet<Node> sortedSet = new TreeSet<>(new KComparator(k));
        sortedSet.addAll(getAllNodes());

        List<Node> closest = new ArrayList<>(r);

        int count = 0;
        for(Node n : sortedSet){
            closest.add(n);
            if(count++ == r){
                break;
            }
        }
        return closest;
    }

    /*
    public synchronized List<Node> findClosestWithLocal(UID k, int r){
        TreeSet<Node> sortedSet = new TreeSet<>(new KComparator(k));
        sortedSet.addAll(getAllNodes());
        sortedSet.add(local.getNode());

        List<Node> closest = new ArrayList<>(r);

        int count = 0;
        for(Node n : sortedSet){
            closest.add(n);
            if(count++ == r){
                break;
            }
        }
        return closest;
    }
    */

    @Override
    public synchronized int getBucketSize(int i){
        return kBuckets[i].size();
    }

    @Override
    public synchronized List<Node> getAllUnqueriedNodes(){
        List<Node> contacts = new ArrayList<>();
        long now = System.currentTimeMillis();

        for(KBucket b : kBuckets){
            contacts.addAll(b.getUnQueriedNodes(now));
        }

        return contacts;
    }

    @Override
    public synchronized void restart(){
        List<Node> nodes = getAllNodes();

        kBuckets = new KBucket[UID.ID_LENGTH*8];
        for(int i = 0; i < UID.ID_LENGTH*8; i++){
            kBuckets[i] = new KBucket();
        }

        for(Node node : nodes){
            insert(node);
        }

        if(listeners.isEmpty()){
            return;
        }

        for(RestartListener listener : listeners){
            listener.onRestart();
        }
    }
}
