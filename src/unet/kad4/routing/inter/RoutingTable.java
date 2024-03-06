package unet.kad4.routing.inter;

import unet.kad4.utils.Node;
import unet.kad4.utils.UID;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public abstract class RoutingTable {

    protected UID uid;
    protected List<RestartListener> listeners;
    protected boolean secureOnly = true;

    public RoutingTable(){
        listeners = new ArrayList<>();
    }

    public abstract void updatePublicIPConsensus(InetAddress source, InetAddress addr);

    public abstract InetAddress getConsensusExternalAddress();

    public abstract void insert(Node n);

    public abstract void deriveUID();

    public UID getDerivedUID(){
        return uid;
    }

    public void addRestartListener(RestartListener listener){
        listeners.add(listener);
    }

    public boolean removeRestartListener(RestartListener listener){
        return listeners.remove(listener);
    }

    public boolean isSecureOnly(){
        return secureOnly;
    }

    public void setSecureOnly(boolean secureOnly){
        this.secureOnly = secureOnly;
    }

    /*
    public synchronized List<Node> getAllNodes(){
        ArrayList<Node> nodes = new ArrayList<>();
        for(KBucket kBucket : kBuckets){
            nodes.addAll(kBucket.getAllNodes());
        }
        return nodes;
    }
    */
    public abstract boolean hasQueried(Node node, long now);

    public abstract int getBucketUID(UID k);

    public abstract List<Node> getAllNodes();

    public abstract List<Node> findClosest(UID k, int r);

    public abstract int getBucketSize(int i);

    public abstract List<Node> getAllUnqueriedNodes();

    public abstract void restart();

    public interface RestartListener {

        void onRestart();
    }
}
