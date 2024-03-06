package unet.kad4.refresh.tasks;

import unet.kad4.messages.FindNodeRequest;
import unet.kad4.messages.FindNodeResponse;
import unet.kad4.messages.PingRequest;
import unet.kad4.refresh.tasks.inter.Task;
import unet.kad4.routing.kb.KBucket;
import unet.kad4.rpc.PingResponseListener;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.StalledEvent;
import unet.kad4.rpc.events.inter.ResponseCallback;
import unet.kad4.utils.Node;
import unet.kad4.utils.UID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BucketRefreshTask extends Task {

    @Override
    public void execute(){
        PingResponseListener listener = new PingResponseListener(getRoutingTable());
        List<Node> queries = new ArrayList<>();
        System.out.println("EXECUTING BUCKET REFRESH");

        for(int i = 1; i < UID.ID_LENGTH; i++){
            if(getRoutingTable().getBucketSize(i) < KBucket.MAX_BUCKET_SIZE){ //IF THE BUCKET IS FULL WHY SEARCH... WE CAN REFILL BY OTHER PEER PINGS AND LOOKUPS...
                final UID k = getRoutingTable().getDerivedUID().generateNodeIdByDistance(i);

                final List<Node> closest = getRoutingTable().findClosest(k, KBucket.MAX_BUCKET_SIZE);
                if(closest.isEmpty()){
                    continue;
                }

                for(Node n : closest){
                    FindNodeRequest request = new FindNodeRequest();
                    request.setDestination(n.getAddress());
                    request.setTarget(k);

                    try{
                        //RequestEvent event = new RequestEvent(request, n);
                        getServer().send(request, n, new ResponseCallback(){
                            @Override
                            public void onResponse(ResponseEvent event){
                                n.setSeen();
                                System.out.println("SEEN FN "+n);
                                //System.out.println(event.getMessage());

                                FindNodeResponse response = (FindNodeResponse) event.getMessage();

                                if(response.hasNodes()){
                                    List<Node> nodes = response.getAllNodes();

                                    long now = System.currentTimeMillis();
                                    for(int i = nodes.size()-1; i > -1; i--){
                                        if(queries.contains(nodes.get(i)) || getRoutingTable().hasQueried(nodes.get(i), now)){
                                            nodes.remove(nodes.get(i));
                                        }
                                    }

                                    queries.addAll(nodes);

                                    for(Node n : nodes){
                                        if((getRoutingTable().isSecureOnly() && !n.hasSecureID()) || n.hasQueried(now)){
                                            System.out.println("SKIPPING "+now+"  "+n.getLastSeen()+"  "+n);
                                            continue;
                                        }

                                        //System.out.println("PINGING "+n);

                                        PingRequest req = new PingRequest();
                                        req.setDestination(n.getAddress());
                                        try{
                                            getServer().send(req, n, listener);
                                        }catch(IOException e){
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onErrorResponse(ResponseEvent event){
                                n.setSeen();
                                //System.err.println("Node sent error message: "+event.getErrorType().getCode()+" - "+message.getErrorType().getDescription());
                            }

                            @Override
                            public void onStalled(StalledEvent event){
                                n.markStale();
                            }
                        });

                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
