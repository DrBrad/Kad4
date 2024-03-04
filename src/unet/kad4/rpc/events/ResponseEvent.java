package unet.kad4.rpc.events;

import unet.kad4.messages.inter.MessageBase;
import unet.kad4.rpc.events.inter.Event;
import unet.kad4.rpc.events.inter.MessageEvent;

public class ResponseEvent extends MessageEvent {

    public ResponseEvent(MessageBase message){
        super(message);
    }
}
