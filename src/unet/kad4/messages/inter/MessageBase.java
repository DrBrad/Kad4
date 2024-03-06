package unet.kad4.messages.inter;

import unet.kad4.libs.bencode.variables.BencodeObject;
import unet.kad4.utils.UID;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import static unet.kad4.Build.VERSION_CODE;

public class MessageBase {

    public static final String TID_KEY = "t";
    protected UID uid;

    protected byte[] tid;
    protected MessageType type;

    protected InetSocketAddress destination, origin, publicAddress;

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

    public MessageType getType(){
        return type;
    }

    public BencodeObject encode(){
        BencodeObject ben = new BencodeObject();

        ben.put(TID_KEY, tid); //TRANSACTION ID
        ben.put("v", VERSION_CODE); //VERSION

        ben.put(MessageType.TYPE_KEY, type.getRPCTypeName());

        return ben;
    }

    public void decode(BencodeObject ben){//throws MessageException {
        if(!ben.containsKey(type.innerKey())){
            //throw new MessageException("Request doesn't contain body", ErrorMessage.ErrorType.PROTOCOL);
        }
        //message.setVersion(ben.getDouble("v"));
    }

    @Override
    public String toString(){
        return encode().toString();
    }
}
