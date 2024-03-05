package unet.kad4.example;

import unet.kad4.libs.bencode.variables.BencodeObject;
import unet.kad4.messages.inter.*;

@Message(method = "get_peers", type = MessageType.REQ_MSG)
public class GetPeersRequest extends MessageBase {

    public GetPeersRequest(byte[] tid){
        super(tid);
    }

    @Override
    public BencodeObject encode(){
        return super.encode();
    }

    @Override
    public void decode(BencodeObject ben){
        super.decode(ben);
    }
}
