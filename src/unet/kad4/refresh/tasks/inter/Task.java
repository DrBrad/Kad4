package unet.kad4.refresh.tasks.inter;

import unet.kad4.Kademlia;
import unet.kad4.Server;
import unet.kad4.refresh.RefreshHandler;
import unet.kad4.routing.inter.RoutingTable;

public abstract class Task {

    private Kademlia kademlia;

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
