package unet.kad4.messages;

import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageBase;
import unet.kad4.messages.inter.MessageType;
import unet.kad4.messages.inter.MethodMessageBase;

@Message(method = "ping", type = MessageType.RSP_MSG)
public class PingResponse extends MethodMessageBase {

    public PingResponse(byte[] tid){
        super(tid);
    }
}
