package unet.kad4.rpc.events;

import unet.kad4.messages.inter.MessageBase;
import unet.kad4.rpc.events.inter.MessageEvent;
import unet.kad4.utils.Node;

public class StalledEvent extends MessageEvent {

    private long sentTime;

    public StalledEvent(MessageBase message){
        super(message);
    }

    public StalledEvent(MessageBase message, Node node){
        super(message, node);
    }

    public void setSentTime(long sentTime){
        this.sentTime = sentTime;
    }

    public long getSentTime(){
        return sentTime;
    }
}
