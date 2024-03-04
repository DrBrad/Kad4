package unet.kad4.messages;

import unet.kad4.libs.bencode.variables.BencodeArray;
import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageBase;
import unet.kad4.messages.inter.MessageType;

//@Message(method = "", type = MessageType.ERR_MSG)
public class ErrorMessage extends MessageBase {

    private ErrorType e;

    public ErrorMessage(byte[] tid){
        super(tid);//, Method.UNKNOWN, Type.ERR_MSG);
    }

    protected void decode(BencodeArray ben){
        if(ben.size() < 2){
            e = ErrorType.PROTOCOL;
        }

        e = ErrorType.fromCode(ben.getInteger(0));
    }

    public ErrorType getErrorType(){
        return e;
    }

    public void setErrorType(ErrorType e){
        this.e = e;
    }

    /*
    @Override
    public BencodeObject getBencode(){
        BencodeObject ben = super.getBencode();
        ben.getBencodeArray(t.innerKey()).add(e.getCode());
        ben.getBencodeArray(t.innerKey()).add(e.getDescription());
        return ben;
    }
    */

    public enum ErrorType {

        /*
		BEP44:
		205	message (v field) too big.
		206	invalid signature
		207	salt (salt field) too big.
		301	the CAS hash mismatched, re-read value and try again.
		302	sequence number less than current.
		*/

        GENERIC {
            @Override
            public int getCode(){
                return 201;
            }
            @Override
            public String getDescription(){
                return "Generic Error";
            }
        }, SERVER {
            @Override
            public int getCode(){
                return 202;
            }
            @Override
            public String getDescription(){
                return "Server Error";
            }
        }, PROTOCOL {
            @Override
            public int getCode(){
                return 203;
            }
            @Override
            public String getDescription(){
                return "Protocol Error, such as a malformed packet, invalid arguments, or bad token";
            }
        }, METHOD {
            @Override
            public int getCode(){
                return 204;
            }
            @Override
            public String getDescription(){
                return "Method Unknown";
            }
        }, INVALID;

        public int getCode(){
            return 0;
        }

        public String getDescription(){
            return name();
        }

        public static ErrorType fromCode(int code){
            for(ErrorType e : values()){
                if(code == e.getCode()){
                    return e;
                }
            }

            throw new IllegalArgumentException("Error type not found.");
        }
    }
}
