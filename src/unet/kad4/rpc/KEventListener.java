package unet.kad4.rpc;

import unet.kad4.Kademlia;
import unet.kad4.messages.FindNodeRequest;
import unet.kad4.messages.FindNodeResponse;
import unet.kad4.messages.PingRequest;
import unet.kad4.messages.PingResponse;
import unet.kad4.routing.kb.KBucket;
import unet.kad4.rpc.events.RequestEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.StalledEvent;
import unet.kad4.rpc.events.inter.EventHandler;
import unet.kad4.utils.Node;
import unet.kad4.utils.UID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static unet.kad4.messages.inter.MessageType.REQ_MSG;
import static unet.kad4.messages.inter.MessageType.RSP_MSG;

public class KEventListener extends EventListener {

    //private List<Node> queries;

    public KEventListener(Kademlia kademlia){
        super(kademlia);
        //queries = new ArrayList<>();
    }

    /*
    @EventHandler
    public void onStalled(StalledEvent event){

    }
    */

    @EventHandler(method = "ping", type = REQ_MSG)
    public void onPingRequest(RequestEvent event){
        if(event.isPreventDefault()){
            return;
        }

        PingResponse response = new PingResponse(event.getMessage().getTransactionID());
        response.setDestination(event.getMessage().getOrigin());
        response.setPublic(event.getMessage().getOrigin());
        event.setResponse(response);
    }

    @EventHandler(method = "ping", type = RSP_MSG)
    public void onPingResponse(ResponseEvent event){
        getRoutingTable().insert(event.getNode());
        System.out.println("SEEN "+event.getNode());
    }

    @EventHandler(method = "find_node", type = REQ_MSG)
    public void onFindNodeRequest(RequestEvent event){
        FindNodeRequest request = (FindNodeRequest) event.getMessage();

        FindNodeResponse response = new FindNodeResponse(request.getTransactionID());
        response.addNodes(getRoutingTable().findClosest(request.getTarget(), KBucket.MAX_BUCKET_SIZE));
        event.setResponse(response);
    }

    @EventHandler(method = "find_node", type = RSP_MSG)
    public void onFindNodeResponse(ResponseEvent event){
        //FindNodeResponse response = (FindNodeResponse) event.getMessage();
        //getRoutingTable().insert(event.getNode());
        System.out.println("SEEN FN "+event.getNode());


        //FindNodeResponse response = (FindNodeResponse) event.getMessage();
        //System.out.println(response);



        /*
        long now = System.currentTimeMillis();
        for(Node node : response.getAllNodes()){
            if(!node.hasSecureID() || node.hasQueried(now)){
                System.out.println("SKIPPING "+now+"  "+node.getLastSeen()+"  "+node);
                continue;
            }

            PingRequest request = new PingRequest();
            request.setDestination(node.getAddress());
            try{
                getServer().send(new RequestEvent(request, node));
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        */
    }
}
