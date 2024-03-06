package unet.kad4.rpc;

import unet.kad4.messages.inter.MessageBase;
import unet.kad4.rpc.events.inter.ResponseCallback;
import unet.kad4.utils.Node;

import static unet.kad4.rpc.ResponseTracker.STALLED_TIME;

public class Call {

    private MessageBase message;
    private Node node;
    private ResponseCallback callback;
    protected long sentTime;

    public Call(MessageBase message, ResponseCallback callback){
        this.message = message;
        this.callback = callback;
        sentTime = System.currentTimeMillis();
    }

    public Call(MessageBase message, Node node, ResponseCallback callback){
        this(message, callback);
        this.node = node;
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

    public boolean hasResponseCallback(){
        return (callback != null);
    }

    public ResponseCallback getResponseCallback(){
        return callback;
    }

    public void setResponseCallback(ResponseCallback callback){
        this.callback = callback;
    }

    public void setSentTime(long sentTime){
        this.sentTime = sentTime;
    }

    public long getSentTime(){
        return sentTime;
    }

    public boolean isStalled(long now){
        return (now-sentTime > STALLED_TIME);
    }
}
