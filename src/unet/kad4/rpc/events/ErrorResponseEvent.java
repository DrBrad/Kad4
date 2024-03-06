package unet.kad4.rpc.events;

import unet.kad4.messages.inter.MessageBase;
import unet.kad4.rpc.events.inter.MessageEvent;

public class ErrorResponseEvent extends MessageEvent {

    public ErrorResponseEvent(MessageBase message){
        super(message);
    }
}
