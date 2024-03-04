package unet.kad4.example;

import unet.kad4.libs.bencode.variables.BencodeObject;
import unet.kad4.messages.inter.*;

@Message(method = "GET_PEERS", type = MessageType.REQ_MSG)
public class GetPeersRequest extends MessageBase {

    public GetPeersRequest(byte[] tid){
        super(tid);//, null, null);
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
