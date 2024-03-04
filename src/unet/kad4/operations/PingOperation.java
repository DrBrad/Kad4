package unet.kad4.operations;

import unet.kad4.messages.ErrorMessage;
import unet.kad4.messages.PingRequest;
import unet.kad4.messages.inter.MessageBase;
import unet.kad4.messages.inter.MessageCallback;
import unet.kad4.operations.inter.Operation;
import unet.kad4.rpc.RPCServer;
import unet.kad4.utils.Node;

import java.util.List;

public class PingOperation {/*implements Operation {

    private RPCServer server;
    private List<Node> nodes;

    public PingOperation(RPCServer server, List<Node> nodes){
        this.server = server;
        this.nodes = nodes;
    }

    @Override
    public void run(){
        long now = System.currentTimeMillis();
        for(Node n : nodes){
            if(!n.hasSecureID() || n.hasQueried(now)){
                System.out.println("SKIPPING "+now+"  "+n.getLastSeen()+"  "+n);
                continue;
            }
            //if(n.hasQueried(now) && n.getStale() == 0){
            //    System.out.println("SKIPPING "+now+"  "+n.getLastSeen()+"  "+n);
            //    continue;
            //}

            PingRequest request = new PingRequest();
            request.setDestination(n.getAddress());

            server.send(new Call(request, new MessageCallback(){
                @Override
                public void onResponse(MessageBase message){
                    if(!n.getUID().equals(message.getUID())){
                        return;
                    }

                    server.getRoutingTable().insert(n);
                    System.out.println("SEEN "+n.getAddress().getHostName());
                }

                @Override
                public void onErrorResponse(ErrorMessage message){
                    if(!n.getUID().equals(message.getUID())){
                        return;
                    }

                    server.getRoutingTable().insert(n);
                    System.out.println("SEEN ER "+n.getAddress().getHostName());
                }

                /*
                @Override
                public void onException(MessageException exception){
                    server.getRoutingTable().insert(n);
                    exception.printStackTrace();
                }
                *./

                @Override
                public void onStalled(){
                    n.markStale();
                    System.err.println("Node stalled: "+n);
                }
            }));
        }
    }*/
}
