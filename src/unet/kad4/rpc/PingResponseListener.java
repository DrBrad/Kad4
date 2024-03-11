package unet.kad4.rpc;

import unet.kad4.routing.inter.RoutingTable;
import unet.kad4.rpc.events.ErrorResponseEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.StalledEvent;
import unet.kad4.rpc.events.inter.ResponseCallback;

public class PingResponseListener extends ResponseCallback {

    private RoutingTable routingTable;

    public PingResponseListener(RoutingTable routingTable){
        this.routingTable = routingTable;
    }

    @Override
    public void onResponse(ResponseEvent event){
        routingTable.insert(event.getNode());
        System.out.println("SEEN "+event.getNode());
    }

    @Override
    public void onErrorResponse(ErrorResponseEvent event){

    }

    @Override
    public void onStalled(StalledEvent event){
        if(event.hasNode()){
            event.getNode().markStale();
        }
    }
}
