package unet.kad4.refresh.tasks.inter;

import unet.kad4.kad.KademliaBase;
import unet.kad4.kad.Server;
import unet.kad4.refresh.RefreshHandler;
import unet.kad4.routing.inter.RoutingTable;

public abstract class Task {

    private KademliaBase kademlia;

    public Server getServer(){
        return kademlia.getServer();
    }

    public RoutingTable getRoutingTable(){
        return kademlia.getRoutingTable();
    }

    public RefreshHandler getRefreshHandler(){
        return kademlia.getRefreshHandler();
    }

    public abstract void execute();
}
