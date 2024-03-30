package unet.kad4.rpc;

import unet.kad4.messages.FindNodeRequest;
import unet.kad4.messages.FindNodeResponse;
import unet.kad4.messages.PingResponse;
import unet.kad4.routing.kb.KBucket;
import unet.kad4.rpc.events.RequestEvent;
import unet.kad4.rpc.events.inter.RequestMapping;
import unet.kad4.utils.Node;

import java.util.List;

public class KRequestListener extends RequestListener {

    @RequestMapping("ping")
    public void onPingRequest(RequestEvent event){
        if(event.isPreventDefault()){
            return;
        }

        PingResponse response = new PingResponse(event.getMessage().getTransactionID());
        response.setDestination(event.getMessage().getOrigin());
        response.setPublic(event.getMessage().getOrigin());
        event.setResponse(response);
    }

    @RequestMapping("find_node")
    public void onFindNodeRequest(RequestEvent event){
        if(event.isPreventDefault()){
            return;
        }

        FindNodeRequest request = (FindNodeRequest) event.getMessage();

        List<Node> nodes = getRoutingTable().findClosest(request.getTarget(), KBucket.MAX_BUCKET_SIZE);
        nodes.remove(event.getNode());

        if(!nodes.isEmpty()){
            FindNodeResponse response = new FindNodeResponse(request.getTransactionID());
            response.setDestination(event.getMessage().getOrigin());
            response.setPublic(event.getMessage().getOrigin());
            response.addNodes(getRoutingTable().findClosest(request.getTarget(), KBucket.MAX_BUCKET_SIZE));
            event.setResponse(response);
        }
    }
}
