package unet.kad4.rpc.events;

import unet.kad4.messages.inter.MessageBase;
import unet.kad4.rpc.events.inter.Event;
import unet.kad4.rpc.events.inter.MessageEvent;
import unet.kad4.utils.Node;

public class ResponseEvent extends MessageEvent {

    private MessageBase request;

    public ResponseEvent(MessageBase message){
        super(message);
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
}
