package unet.kad4.messages;

import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageBase;
import unet.kad4.messages.inter.MessageType;

@Message(method = "ping", type = MessageType.RSP_MSG)
public class PingResponse extends MessageBase {

    public PingResponse(byte[] tid){
        super(tid);
    }
}
