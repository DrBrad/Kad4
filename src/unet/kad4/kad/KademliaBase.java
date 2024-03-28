package unet.kad4.kad;

import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageBase;
import unet.kad4.messages.inter.MessageKey;
import unet.kad4.refresh.RefreshHandler;
import unet.kad4.routing.BucketTypes;
import unet.kad4.routing.inter.RoutingTable;
import unet.kad4.utils.Node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class KademliaBase {

    protected RoutingTable routingTable;
    protected Server server;
    protected RefreshHandler refresh;

    public KademliaBase(){
        this(BucketTypes.KADEMLIA.getRoutingTable());
    }

    public KademliaBase(String bucketType){
        this(BucketTypes.fromString(bucketType).getRoutingTable());
    }

    public KademliaBase(RoutingTable routingTable){
        this.routingTable = routingTable;
        System.out.println("Starting with bucket type: "+routingTable.getClass().getSimpleName());
        server = new Server(this);
        refresh = new RefreshHandler(this);
    }

    public void join(int localPort, InetAddress address, int port)throws IOException {
        join(localPort, new InetSocketAddress(address, port));
    }

    public void join(int localPort, Node node)throws IOException {
        join(localPort, node.getAddress());
    }

    public void join(int localPort, InetSocketAddress address)throws IOException {
        if(!server.isRunning()){
            server.start(localPort);
        }
    }

    public void bind()throws SocketException {
        bind(0);
    }

    public void bind(int port)throws SocketException {
        if(!server.isRunning()){
            server.start(port);
        }
    }

    public void stop(){
        server.stop();
        refresh.stop();
    }

    public Server getServer(){
        return server;
    }

    public RoutingTable getRoutingTable(){
        return routingTable;
    }

    public RefreshHandler getRefreshHandler(){
        return refresh;
    }
}
