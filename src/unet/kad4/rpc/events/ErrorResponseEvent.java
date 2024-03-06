package unet.kad4.rpc.events;

import unet.kad4.messages.inter.MessageBase;
import unet.kad4.rpc.events.inter.MessageEvent;
import unet.kad4.utils.Node;

public class ErrorResponseEvent extends MessageEvent {

    private MessageBase request;
    private long sentTime;

    public ErrorResponseEvent(MessageBase message){
        super(message);
    }

    public ErrorResponseEvent(MessageBase message, Node node){
        super(message, node);
    }

    public boolean hasRequest(){
        return (request != null);
    }

    public void setRequest(MessageBase request){
        this.request = request;
    }

    public MessageBase getRequest(){
        return request;
    }

    public void setSentTime(long sentTime){
        this.sentTime = sentTime;
    }

    public long getSentTime(){
        return sentTime;
    }
}
