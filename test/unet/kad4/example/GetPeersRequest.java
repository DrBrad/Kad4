package unet.kad4.example;

import unet.bencode.variables.BencodeObject;
import unet.kad4.messages.inter.*;

@Message(method = "get_peers", type = MessageType.REQ_MSG)
public class GetPeersRequest extends MethodMessageBase {

    public GetPeersRequest(byte[] tid){
        super(tid);
    }

    @Override
    public BencodeObject encode(){
        return super.encode();
    }

    @Override
    public void decode(BencodeObject ben)throws MessageException {
        super.decode(ben);
    }
}
