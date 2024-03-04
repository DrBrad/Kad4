package unet.kad4.messages;

import unet.kad4.libs.bencode.variables.BencodeObject;
import unet.kad4.messages.inter.MessageBase;
import unet.kad4.messages.inter.MessageException;
import unet.kad4.messages.inter.MessageType;

//import static unet.kad4.messages.inter.MessageBase.Method.FIND_NODE;


public class MessageDecoder {

    //REQUEST
    /*
    {
        "t":"aa",
        "y":"q",
        "q":"ping",
        "a":{
            "id":"abcdefghij0123456789"
        }
    }
    */

    //RESPONSE
    /*
    {
        "t":"aa",
        "y":"r",
        "r":{
            "id":"mnopqrstuvwxyz123456"
        },
        "ip": bytes
    }
    */

    //WE SOMEHOW SHOULD PASS THE IP AND PORT OF WHO SENT IT...

    private byte[] tid;
    //private MessageBase.Type type;
    private BencodeObject ben;

    public MessageDecoder(byte[] b)throws MessageException {
        /*
        ben = new BencodeObject(b);
        if(!ben.containsKey("t") || !ben.containsKey(Type.TYPE_KEY)){
            throw new MessageException("Required keys are missing", ErrorMessage.ErrorType.PROTOCOL);
        }

        tid = ben.getBytes("t");
        type = Type.fromRPCTypeName(ben.getString(Type.TYPE_KEY));
        */
    }

    public byte[] getTransactionID(){
        return tid;
    }

    public MessageType getType(){
        return null;//type;
    }

    public MessageBase decodeRequest()throws MessageException {
        /*
        if(!ben.containsKey(type.innerKey())){
            throw new MessageException("Request doesn't contain body", ErrorMessage.ErrorType.PROTOCOL);
        }

        if(!ben.getBencodeObject(type.innerKey()).containsKey("id")){
            throw new MessageException("Request doesn't contain UID", ErrorMessage.ErrorType.PROTOCOL);
        }

        MessageBase message;
        MessageBase.Method m = MessageBase.Method.fromRPCName(ben.getString(type.getRPCTypeName()));

        switch(m){
            case PING:
                message = new PingRequest(tid);
                break;

            case FIND_NODE:
                if(!ben.getBencodeObject(type.innerKey()).containsKey("target")){
                    throw new MessageException("Request of "+FIND_NODE+" did not contain 'target'", ErrorMessage.ErrorType.PROTOCOL);
                }

                message = new FindNodeRequest(tid);
                ((FindNodeRequest) message).setTarget(new UID(ben.getBencodeObject(type.innerKey()).getBytes("target")));
                break;

            default:
                throw new MessageException("Request is using "+m+" method", ErrorMessage.ErrorType.METHOD);
        }

        message.setUID(new UID(ben.getBencodeObject(type.innerKey()).getBytes("id")));
        //message.setVersion(ben.getDouble("v"));

        return message;
        */
        return null;
    }

    public MessageBase decodeResponse(/*MessageBase.Method method*/)throws MessageException {
        /*
        if(!ben.containsKey(type.innerKey())){
            throw new MessageException("Request doesn't contain body", ErrorMessage.ErrorType.PROTOCOL);
        }

        if(!ben.getBencodeObject(type.innerKey()).containsKey("id")){
            throw new MessageException("Request doesn't contain UID", ErrorMessage.ErrorType.PROTOCOL);
        }

        MessageBase message;

        //try{
        switch(method){
            case PING:
                message = new PingResponse(tid);
                break;

            case FIND_NODE:
                if(!ben.getBencodeObject(type.innerKey()).containsKey("nodes") &&
                        !ben.getBencodeObject(type.innerKey()).containsKey("nodes6")){
                    throw new MessageException("Response to "+FIND_NODE+" did not contain 'node' or 'node6'", ErrorMessage.ErrorType.PROTOCOL);
                }

                message = new FindNodeResponse(tid);

                if(ben.getBencodeObject(type.innerKey()).containsKey("nodes")){
                    ((FindNodeResponse) message).addNodes(unpackNodes(ben.getBencodeObject(type.innerKey()).getBytes("nodes"), AddressType.IPv4), AddressType.IPv4);
                }

                if(ben.getBencodeObject(type.innerKey()).containsKey("nodes6")){
                    ((FindNodeResponse) message).addNodes(unpackNodes(ben.getBencodeObject(type.innerKey()).getBytes("nodes6"), AddressType.IPv6), AddressType.IPv6);
                }

                break;

            case GET:

            case PUT:

            default:
                return new MessageBase(tid, method, MessageBase.Type.INVALID);
        }

        message.setUID(new UID(ben.getBencodeObject(type.innerKey()).getBytes("id")));

        if(ben.containsKey("ip")){
            message.setPublic(AddressUtils.unpackAddress(ben.getBytes("ip")));
        }

        //message.setVersion(ben.getDouble("v"));
        //}catch(){
        //    message = new ErrorMessage(tid);
        //}

        return message;
        */
        return null;
    }

    public ErrorMessage decodeError()throws MessageException {
        /*
        if(!ben.containsKey(type.innerKey())){
            throw new MessageException("Request doesn't contain body", ErrorMessage.ErrorType.PROTOCOL);
        }

        ErrorMessage message = new ErrorMessage(tid);
        message.decode(ben.getBencodeArray(type.innerKey()));

        if(ben.containsKey("ip")){
            message.setPublic(AddressUtils.unpackAddress(ben.getBytes("ip")));
        }
        return message;
        */
        return null;
    }
}
