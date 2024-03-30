package unet.kad4.messages;

import unet.bencode.variables.BencodeArray;
import unet.bencode.variables.BencodeObject;
import unet.kad4.messages.inter.MessageBase;
import unet.kad4.messages.inter.MessageException;
import unet.kad4.messages.inter.MessageType;
import unet.kad4.utils.net.AddressUtils;

public class ErrorResponse extends MessageBase {

    /*
		BEP44:
		205	message (v field) too big.
		206	invalid signature
		207	salt (salt field) too big.
		301	the CAS hash mismatched, re-read value and try again.
		302	sequence number less than current.
	*/

    private int code;
    private String description;

    public ErrorResponse(){
        type = MessageType.ERR_MSG;
    }

    public ErrorResponse(byte[] tid){
        this();
        this.tid = tid;
    }

    @Override
    public BencodeObject encode(){
        BencodeObject ben = super.encode();

        BencodeArray arr = new BencodeArray();
        arr.add(code);
        arr.add(description);

        ben.put(type.innerKey(), arr);

        //NOT SURE IF WE PASS IP...
        if(publicAddress != null){
            ben.put("ip", AddressUtils.packAddress(publicAddress)); //PACK MY IP ADDRESS
        }

        return ben;
    }

    @Override
    public void decode(BencodeObject ben)throws MessageException {
        super.decode(ben);

        if(ben.getBencodeArray(type.innerKey()).size() < 2){
            throw new MessageException("Protocol Error, such as a malformed packet.", 203);
        }

        code = ben.getBencodeArray(type.innerKey()).getInteger(0);
        description = ben.getBencodeArray(type.innerKey()).getString(1);
    }

    public void setCode(int code){
        this.code = code;
    }

    public int getCode(){
        return code;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}
