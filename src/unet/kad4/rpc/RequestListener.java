package unet.kad4.rpc;

import unet.kad4.Kademlia;
import unet.kad4.Server;
import unet.kad4.routing.inter.RoutingTable;

public class RequestListener {

    private Kademlia kademlia;

    //public RequestListener(Kademlia kademlia){
    //    this.kademlia = kademlia;
    //}

    private void setKademlia(Kademlia kademlia){
        this.kademlia = kademlia;
    }

    public Kademlia getKademlia(){
        return kademlia;
    }

    public Server getServer(){
        return kademlia.getServer();
    }

    public RoutingTable getRoutingTable(){
        return kademlia.getRoutingTable();
    }
}
