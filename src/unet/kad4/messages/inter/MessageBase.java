package unet.kad4.messages.inter;

import unet.kad4.libs.bencode.variables.BencodeArray;
import unet.kad4.libs.bencode.variables.BencodeObject;
import unet.kad4.utils.UID;
import unet.kad4.utils.net.AddressUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import static unet.kad4.Build.VERSION_CODE;

public class MessageBase {

    public static final String TID_KEY = "t";
    protected UID uid;
    //protected String m;
    //protected Type t;

    protected byte[] tid;

    protected InetSocketAddress destination, origin, publicAddress;

    protected Message message;

    //TRANSACTION ID
    //VERSION... "DONT HARD CODE THIS SHIT..."
    //WHERE TF ARE WE GOING TO SEND THE ID...
    //      THE RPC SERVER WILL SET THE TID AND THE UID...

    public MessageBase(){
        if(getClass().isAnnotationPresent(Message.class)){
            message = getClass().getAnnotation(Message.class);
        }
    }

    public MessageBase(byte[] tid){
        this();
        this.tid = tid;
        //m = getClass().getAnnotation(Message.class).method();
        //t = getClass().getAnnotation(Message.class).type();
    }

    /*
    public MessageBase(byte[] tid, Method m, Type t){
        this.tid = tid;
        this.m = m;
        this.t = t;
    }
    */

    /*
    public BencodeObject encode(BencodeObject ben){

    }

    public void decode(BencodeObject ben){

    }
    */



    //public abstract BencodeObject getBencode();//{
        /*
        BencodeObject ben = new BencodeObject();
        ben.put("t", tid); //TRANSACTION ID
        ben.put("v", VERSION_CODE); //VERSION

        ben.put(Type.TYPE_KEY, t.getRPCTypeName());

        switch(t){
            case REQ_MSG:
                ben.put(t.getRPCTypeName(), m.getRPCName());
                ben.put(t.innerKey(), new BencodeObject());
                ben.getBencodeObject(t.innerKey()).put("id", uid.getBytes());
                break;

            case RSP_MSG:
                ben.put(t.innerKey(), new BencodeObject());
                ben.getBencodeObject(t.innerKey()).put("id", uid.getBytes());

                if(publicAddress != null){
                    ben.put("ip", AddressUtils.packAddress(publicAddress)); //PACK MY IP ADDRESS
                }
                break;

            case ERR_MSG:
                ben.put(t.innerKey(), new BencodeArray());

                //NOT SURE IF WE PASS IP...
                if(publicAddress != null){
                    ben.put("ip", AddressUtils.packAddress(publicAddress)); //PACK MY IP ADDRESS
                }
                break;
        }

        return ben;
        */
    //    return null;
    //}

    public void setUID(UID uid){
        this.uid = uid;
    }

    public UID getUID(){
        return uid;
    }

    public void setTransactionID(byte[] tid){
        this.tid = tid;
    }

    public byte[] getTransactionID(){
        return tid;
    }

    public InetSocketAddress getPublic(){
        return publicAddress;
    }

    public InetAddress getPublicAddress(){
        return publicAddress.getAddress();
    }

    public int getPublicPort(){
        return publicAddress.getPort();
    }

    public void setPublic(InetAddress address, int port){
        publicAddress = new InetSocketAddress(address, port);
    }

    public void setPublic(InetSocketAddress publicAddress){
        this.publicAddress = publicAddress;
    }

    public InetSocketAddress getDestination(){
        return destination;
    }

    public InetAddress getDestinationAddress(){
        return destination.getAddress();
    }

    public int getDestinationPort(){
        return destination.getPort();
    }

    public void setDestination(InetAddress address, int port){
        destination = new InetSocketAddress(address, port);
    }

    public void setDestination(InetSocketAddress destination){
        this.destination = destination;
    }

    public InetSocketAddress getOrigin(){
        return origin;
    }

    public InetAddress getOriginAddress(){
        return origin.getAddress();
    }

    public int getOriginPort(){
        return origin.getPort();
    }

    public void setOrigin(InetAddress address, int port){
        origin = new InetSocketAddress(address, port);
    }

    public void setOrigin(InetSocketAddress origin){
        this.origin = origin;
    }

    public String getMethod(){
        return message.method();
    }

    public MessageType getType(){
        return message.type();
    }

    public BencodeObject encode(){
        BencodeObject ben = new BencodeObject();

        ben.put(TID_KEY, tid); //TRANSACTION ID
        ben.put("v", VERSION_CODE); //VERSION

        ben.put(MessageType.TYPE_KEY, message.type().getRPCTypeName());

        switch(message.type()){
            case REQ_MSG:
                ben.put(message.type().getRPCTypeName(), message.method());
                ben.put(message.type().innerKey(), new BencodeObject());
                ben.getBencodeObject(message.type().innerKey()).put("id", uid.getBytes());
                break;

            case RSP_MSG:
                ben.put(message.type().innerKey(), new BencodeObject());
                ben.getBencodeObject(message.type().innerKey()).put("id", uid.getBytes());

                if(publicAddress != null){
                    ben.put("ip", AddressUtils.packAddress(publicAddress)); //PACK MY IP ADDRESS
                }
                break;

            case ERR_MSG:
                ben.put(message.type().innerKey(), new BencodeArray());

                //NOT SURE IF WE PASS IP...
                if(publicAddress != null){
                    ben.put("ip", AddressUtils.packAddress(publicAddress)); //PACK MY IP ADDRESS
                }
                break;
        }

        return ben;
    }

    public void decode(BencodeObject ben){//throws MessageException {
        if(!ben.containsKey(message.type().innerKey())){
            //throw new MessageException("Request doesn't contain body", ErrorMessage.ErrorType.PROTOCOL);
        }

        if(!ben.getBencodeObject(message.type().innerKey()).containsKey("id")){
            //throw new MessageException("Request doesn't contain UID", ErrorMessage.ErrorType.PROTOCOL);
        }

        uid = new UID(ben.getBencodeObject(message.type().innerKey()).getBytes("id"));

        switch(message.type()){
            case RSP_MSG:
            case ERR_MSG:
                if(ben.containsKey("ip")){
                    publicAddress = AddressUtils.unpackAddress(ben.getBytes("ip"));
                }
                break;
        }
        //message.setVersion(ben.getDouble("v"));
    }

    @Override
    public String toString(){
        //byte[] b = MessageBase.Encoder.encode(new PingRequest(null));
        return encode().toString();
    }

    /*
    public class Encoder {

        public static byte[] encode(MessageBase message){
            BencodeObject ben = new BencodeObject();
            ben.put("t", message.tid); //TRANSACTION ID
            ben.put("v", VERSION_CODE); //VERSION

            Message a = message.getClass().getAnnotation(Message.class);
            Type t = a.type();
            String m = a.method();

            ben.put(Type.TYPE_KEY, t.getRPCTypeName());

            switch(t){
                case REQ_MSG:
                    ben.put(t.getRPCTypeName(), m);
                    ben.put(t.innerKey(), new BencodeObject());
                    ben.getBencodeObject(t.innerKey()).put("id", message.uid.getBytes());
                    break;

                case RSP_MSG:
                    ben.put(t.innerKey(), new BencodeObject());
                    ben.getBencodeObject(t.innerKey()).put("id", message.uid.getBytes());

                    if(message.publicAddress != null){
                        ben.put("ip", AddressUtils.packAddress(message.publicAddress)); //PACK MY IP ADDRESS
                    }
                    break;

                case ERR_MSG:
                    ben.put(t.innerKey(), new BencodeArray());

                    //NOT SURE IF WE PASS IP...
                    if(message.publicAddress != null){
                        ben.put("ip", AddressUtils.packAddress(message.publicAddress)); //PACK MY IP ADDRESS
                    }
                    break;
            }

            return ben.encode();
        }
    }
    */
}
