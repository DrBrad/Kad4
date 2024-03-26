package unet.kad4.rpc;

import unet.kad4.Kademlia;
import unet.kad4.kad.KademliaBase;
import unet.kad4.messages.FindNodeResponse;
import unet.kad4.messages.PingRequest;
import unet.kad4.rpc.events.ErrorResponseEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.StalledEvent;
import unet.kad4.rpc.events.inter.ResponseCallback;
import unet.kad4.utils.Node;

import java.io.IOException;
import java.util.List;

public class JoinNodeListener extends ResponseCallback {

    private KademliaBase kademlia;

    public JoinNodeListener(KademliaBase kademlia){
        this.kademlia = kademlia;
    }

    @Override
    public void onResponse(ResponseEvent event){
        kademlia.getRoutingTable().insert(event.getNode());
        System.out.println("JOINED "+event.getNode());

        FindNodeResponse response = (FindNodeResponse) event.getMessage();

        if(response.hasNodes()){
            List<Node> nodes = response.getAllNodes();

            PingResponseListener listener = new PingResponseListener(kademlia.getRoutingTable());

            long now = System.currentTimeMillis();
            for(Node n : nodes){
                if((kademlia.getRoutingTable().isSecureOnly() && !n.hasSecureID()) || n.hasQueried(now)){
                    System.out.println("SKIPPING "+now+"  "+n.getLastSeen()+"  "+n);
                    continue;
                }

                PingRequest req = new PingRequest();
                req.setDestination(n.getAddress());
                try{
                    kademlia.getServer().send(req, n, listener);
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }

        if(!kademlia.getRefreshHandler().isRunning()){
            kademlia.getRefreshHandler().start();
        }
    }

    @Override
    public void onErrorResponse(ErrorResponseEvent event){

    }

    @Override
    public void onStalled(StalledEvent event){

    }
}
