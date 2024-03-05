package unet.kad4.refresh.tasks;

import unet.kad4.Kademlia;
import unet.kad4.messages.PingRequest;
import unet.kad4.refresh.tasks.inter.Task;
import unet.kad4.rpc.events.RequestEvent;
import unet.kad4.utils.Node;

import java.io.IOException;
import java.util.List;

public class StaleRefreshTask extends Task {

    public StaleRefreshTask(Kademlia kademlia){
        super(kademlia);
    }

    @Override
    public void execute(){
        List<Node> nodes = getRoutingTable().getAllUnqueriedNodes();

        for(Node node : nodes){
            PingRequest request = new PingRequest();
            request.setDestination(node.getAddress());

            try{
                getServer().send(new RequestEvent(request, node));
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
