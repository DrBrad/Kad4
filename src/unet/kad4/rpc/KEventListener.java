package unet.kad4.rpc;

import unet.kad4.Kademlia;
import unet.kad4.messages.FindNodeRequest;
import unet.kad4.messages.FindNodeResponse;
import unet.kad4.messages.PingResponse;
import unet.kad4.messages.inter.MessageType;
import unet.kad4.routing.kb.KBucket;
import unet.kad4.rpc.events.RequestEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.inter.EventHandler;

public class KEventListener extends EventListener {

    public KEventListener(Kademlia kademlia){
        super(kademlia);
    }

    @EventHandler(method = "ping", type = MessageType.REQ_MSG)
    public void onPingRequest(RequestEvent event){
        if(event.isPreventDefault()){
            return;
        }

        PingResponse response = new PingResponse(event.getMessage().getTransactionID());
        response.setDestination(event.getMessage().getOrigin());
        response.setPublic(event.getMessage().getOrigin());
        event.setResponse(response);
    }

    @EventHandler(method = "ping", type = MessageType.RSP_MSG)
    public void onPingResponse(ResponseEvent event){
        //getRoutingTable().insert(event.getNode());
    }

    @EventHandler(method = "find_node", type = MessageType.REQ_MSG)
    public void onFindNodeRequest(RequestEvent event){
        FindNodeRequest request = (FindNodeRequest) event.getMessage();

        FindNodeResponse response = new FindNodeResponse(request.getTransactionID());
        response.addNodes(getRoutingTable().findClosest(request.getTarget(), KBucket.MAX_BUCKET_SIZE));
        event.setResponse(response);
    }

    @EventHandler(method = "find_node", type = MessageType.RSP_MSG)
    public void onFindNodeResponse(ResponseEvent event){
        FindNodeResponse response = (FindNodeResponse) event.getMessage();
        //getRoutingTable().insert(event.getNode());
        System.out.println(response);

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
