package unet.kad4;

import unet.kad4.kad.KademliaBase;
import unet.kad4.messages.FindNodeRequest;
import unet.kad4.messages.FindNodeResponse;
import unet.kad4.messages.PingRequest;
import unet.kad4.messages.PingResponse;
import unet.kad4.refresh.tasks.BucketRefreshTask;
import unet.kad4.refresh.tasks.StaleRefreshTask;
import unet.kad4.routing.BucketTypes;
import unet.kad4.routing.inter.RoutingTable;
import unet.kad4.rpc.JoinNodeListener;
import unet.kad4.rpc.KRequestListener;

import java.io.IOException;
import java.lang.reflect.*;
import java.net.InetSocketAddress;

public class Kademlia extends KademliaBase {

    public Kademlia(){
        this(BucketTypes.KADEMLIA.getRoutingTable());
    }

    public Kademlia(String bucketType){
        this(BucketTypes.fromString(bucketType).getRoutingTable());
    }

    public Kademlia(RoutingTable routingTable){
        super(routingTable);

        BucketRefreshTask bucketRefreshTask = new BucketRefreshTask();

        routingTable.addRestartListener(new RoutingTable.RestartListener(){
            @Override
            public void onRestart(){
                bucketRefreshTask.execute();
            }
        });

        try{
            registerRequestListener(new KRequestListener());

            registerMessage(PingRequest.class);
            registerMessage(PingResponse.class);
            registerMessage(FindNodeRequest.class);
            registerMessage(FindNodeResponse.class);

            refresh.addOperation(bucketRefreshTask);
            refresh.addOperation(new StaleRefreshTask());

        }catch(NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
    }

    public void join(int localPort, InetSocketAddress address)throws IOException {
        super.join(localPort, address);
        FindNodeRequest request = new FindNodeRequest();
        request.setDestination(address);
        request.setTarget(routingTable.getDerivedUID());
        server.send(request, new JoinNodeListener(this));
    }
}
