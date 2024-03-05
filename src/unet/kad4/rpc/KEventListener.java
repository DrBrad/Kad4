package unet.kad4.rpc;

import unet.kad4.Kademlia;
import unet.kad4.messages.FindNodeRequest;
import unet.kad4.messages.FindNodeResponse;
import unet.kad4.messages.PingResponse;
import unet.kad4.routing.kb.KBucket;
import unet.kad4.rpc.events.RequestEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.StalledEvent;
import unet.kad4.rpc.events.inter.EventHandler;
import unet.kad4.utils.Node;
import unet.kad4.utils.UID;

import java.util.ArrayList;
import java.util.List;

import static unet.kad4.messages.inter.MessageType.REQ_MSG;
import static unet.kad4.messages.inter.MessageType.RSP_MSG;

public class KEventListener extends EventListener {

    public KEventListener(Kademlia kademlia){
        super(kademlia);
    }

    @EventHandler
    public void onBucketRefresh(){
        List<Node> queries = new ArrayList<>();

        for(int i = 1; i < UID.ID_LENGTH; i++){
            if(getRoutingTable().getBucketSize(i) < KBucket.MAX_BUCKET_SIZE){ //IF THE BUCKET IS FULL WHY SEARCH... WE CAN REFILL BY OTHER PEER PINGS AND LOOKUPS...
                final UID k = getRoutingTable().getDerivedUID().generateNodeIdByDistance(i);

                final List<Node> closest = getRoutingTable().findClosest(k, KBucket.MAX_BUCKET_SIZE);
                if(!closest.isEmpty()){
                    for(Node n : closest){
                        FindNodeRequest request = new FindNodeRequest();
                        request.setDestination(n.getAddress());
                        request.setTarget(k);

                        /*
                        server.send(new Call(request, new MessageCallback(){
                            @Override
                            public void onResponse(MessageBase message){
                                if(!n.getUID().equals(message.getUID())){
                                    return;
                                }

                                n.setSeen();

                                System.out.println("SEEN FN "+message.getOrigin());
                                FindNodeResponse r = (FindNodeResponse) message;

                                //queries.addAll(r.getAllNodes());

                                List<Node> nodes = r.getAllNodes();
                                for(int i = nodes.size()-1; i > -1; i--){
                                    if(queries.contains(nodes.get(i))){
                                        nodes.remove(nodes.get(i));
                                    }
                                }

                                /*
                                //queries.addAll(nodes);
                                for(Node n : r.getAllNodes()){
                                    server.getRoutingTable().insert(n);
                                    n.markStale();
                                }

                                new PingOperation(server, r.getAllNodes()).run();
                                *./
                                queries.addAll(nodes);

                                new PingOperation(server, nodes).run();
                            }

                            @Override
                            public void onErrorResponse(ErrorMessage message){
                                if(!n.getUID().equals(message.getUID())){
                                    return;
                                }

                                n.setSeen();
                                System.err.println("Node sent error message: "+message.getErrorType().getCode()+" - "+message.getErrorType().getDescription());
                            }

                            /*
                            @Override
                            public void onException(MessageException exception){
                                n.setSeen();
                                exception.printStackTrace();
                            }
                            *./

                            @Override
                            public void onStalled(){
                                n.markStale();
                                System.err.println("Node stalled: "+n);
                            }
                        }));
                        */
                    }
                }
            }
        }
    }

    @EventHandler
    public void onStalled(StalledEvent event){

    }

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
        //getRoutingTable().insert(event.getNode());
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
