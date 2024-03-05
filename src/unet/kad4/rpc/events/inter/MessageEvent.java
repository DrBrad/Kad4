package unet.kad4.rpc.events.inter;

import unet.kad4.messages.inter.MessageBase;
import unet.kad4.utils.Node;

import static unet.kad4.rpc.ResponseTracker.STALLED_TIME;

public class MessageEvent extends Event {

    private MessageBase message;
    private Node node;
    private long sentTime, receivedTime;

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
