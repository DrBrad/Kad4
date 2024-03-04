package unet.kad4.messages;

import unet.kad4.libs.bencode.variables.BencodeObject;
import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageBase;
import unet.kad4.messages.inter.MessageType;
import unet.kad4.utils.UID;

@Message(method = "find_node", type = MessageType.REQ_MSG)
public class FindNodeRequest extends MessageBase {

    private UID target;

    public FindNodeRequest(){
    }

    public FindNodeRequest(byte[] tid){
        super(tid);
    }

    @Override
    public BencodeObject encode(){
        BencodeObject ben = super.encode();
        ben.getBencodeObject(message.type().innerKey()).put("target", target.getBytes());
        return ben;
    }

    @Override
    public void decode(BencodeObject ben){
        super.decode(ben);
        if(!ben.getBencodeObject(message.type().innerKey()).containsKey("target")){
            System.out.println("MISSING TARGET");
        }

        target = new UID(ben.getBencodeObject(message.type().innerKey()).getBytes("target"));
    }

    public UID getTarget(){
        return target;
    }

    public void setTarget(UID target){
        this.target = target;
    }
}
