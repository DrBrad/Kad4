package unet.kad4.rpc.events;

import unet.kad4.messages.inter.MessageBase;
import unet.kad4.rpc.events.inter.MessageEvent;

public class StalledEvent extends MessageEvent {

    public StalledEvent(MessageBase message){
        super(message);
    }
}
