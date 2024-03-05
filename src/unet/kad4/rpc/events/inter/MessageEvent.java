package unet.kad4.rpc.events.inter;

import unet.kad4.messages.inter.MessageBase;
import unet.kad4.routing.inter.RoutingTable;
import unet.kad4.Server;
import unet.kad4.utils.Node;

import static unet.kad4.rpc.ResponseTracker.STALLED_TIME;

public class MessageEvent extends Event {

    protected MessageBase message;
    protected Node node;
    protected long sentTime, receivedTime;
    protected Server server;
    protected RoutingTable routingTable;

    public MessageEvent(MessageBase message){
        this.message = message;
    }

    public MessageBase getMessage(){
        return message;
    }

    public boolean hasNode(){
        return (node != null);
    }

    public void setNode(Node node){
        this.node = node;
    }

    public Node getNode(){
        return node;
    }

    public void setSentTime(long sentTime){
        this.sentTime = sentTime;
    }

    public long getSentTime(){
        return sentTime;
    }

    public void setReceivedTime(long receivedTime){
        this.receivedTime = receivedTime;
    }

    public long getReceivedTime(){
        return receivedTime;
    }

    public void setServer(Server server){
        this.server = server;
    }

    public Server getServer(){
        return server;
    }

    public void setRoutingTable(RoutingTable routingTable){
        this.routingTable = routingTable;
    }

    public RoutingTable getRoutingTable(){
        return routingTable;
    }

    public boolean isStalled(long now){
        return (now-sentTime > STALLED_TIME);
    }

    public void sent(){
        sentTime = System.currentTimeMillis();
    }

    public void received(){
        receivedTime = System.currentTimeMillis();
    }
}
