package unet.kad4.routing.mainline;

import unet.kad4.utils.hash.CRC32c;
import unet.kad4.routing.inter.RoutingTable;
import unet.kad4.utils.Node;
import unet.kad4.utils.UID;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static unet.kad4.utils.Node.*;

public class MRoutingTable extends RoutingTable {

    //TODO

    private ArrayList<MBucket> mBuckets = new ArrayList<>();
    private InetAddress consensusExternalAddress;

    public MRoutingTable(){
        try{
            consensusExternalAddress = Inet4Address.getLocalHost();
        }catch(UnknownHostException e){
            e.printStackTrace();
        }

        deriveUID();

        mBuckets.add(new MBucket()); //CLOSEST
        mBuckets.add(new MBucket()); //FURTHEST
    }

    @Override
    public void updatePublicIPConsensus(InetAddress source, InetAddress addr){
    }

    @Override
    public InetAddress getConsensusExternalAddress(){
        return null;
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
        if(n.hasSecureID()){ //NODE VERIFICATION CHECK
            if(!uid.equals(n.getUID())){ //SELF CHECK
                int b = getBucketUID(n.getUID());

                //ARE WE EVEN CHECKING FOR IP/PORT MATCHES...

                if(b < mBuckets.size()){
                    if(!mBuckets.get(b).isFull()){
                        mBuckets.get(b).insert(n);
                    }//ELSE...???? - I GUESS THIS IS FINE BECAUSE THIS SHOULD ALREADY BE FULL AFTER SPLIT...

                }else{

                    //LOOK INTO THIS....
                    //NOT SURE IF THIS IS DONE VERY WELL...
                    if(mBuckets.get(mBuckets.size()-1).isFull()){
                        mBuckets.add(mBuckets.size(), new MBucket());

                        //SPLIT TIME
                        for(int i = mBuckets.get(mBuckets.size()-2).size()-1; i > -1; i--){
                            //for(Node ns : buckets.get(buckets.size()-2).list()){
                            Node ns = mBuckets.get(mBuckets.size()-2).get(i);
                            int j = getBucketUID(ns.getUID());
                            if(j == mBuckets.size()-1){
                                mBuckets.get(mBuckets.size()-2).remove(ns);
                                mBuckets.get(mBuckets.size()-1).insert(ns);
                            }
                        }
                        System.out.println("SPLIT ("+(mBuckets.size()-2)+" - "+(mBuckets.size()-1)+")");
                    }

                    if(!mBuckets.get(mBuckets.size()-1).isFull()){
                        mBuckets.get(mBuckets.size()-1).insert(n);
                    }

                }


            }
        }
    }


    @Override
    public synchronized boolean hasQueried(Node node, long now){
        return false;
    }

    @Override
    public synchronized int getBucketUID(UID k){
        return uid.getDistance(k);//-1;
    }

    /*
    public synchronized List<Node> getAllNodes(){
        ArrayList<Node> nodes = new ArrayList<>();
        for(KBucket kBucket : kBuckets){
            nodes.addAll(kBucket.getAllNodes());
        }
        return nodes;
    }

    public synchronized int getBucketId(UID k){
        int bid = uid.getDistance(k)-1;
        return bid < 0 ? 0 : bid;
    }
    */


    @Override
    public synchronized List<Node> getAllNodes(){
        return null;
    }

    @Override
    public synchronized List<Node> findClosest(UID k, int r){
        return null;
    }

    @Override
    public synchronized int getBucketSize(int i){
        return mBuckets.get(i).size();
    }

    @Override
    public synchronized List<Node> getAllUnqueriedNodes(){
        return null;
    }

    @Override
    public void restart(){

    }
}
