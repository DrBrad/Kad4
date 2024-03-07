package unet.kad4.rpc;

import unet.kad4.Kademlia;
import unet.kad4.Server;
import unet.kad4.routing.inter.RoutingTable;

public class RequestListener {

    private Kademlia kademlia;

    public Server getServer(){
        return kademlia.getServer();
    }

    public RoutingTable getRoutingTable(){
        return kademlia.getRoutingTable();
    }
}
