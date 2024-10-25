package unet.kad4.messages.inter;

public class MessageKey {

    private String method;
    private MessageType type;

    public MessageKey(String method, MessageType type){
        this.method = method;
        this.type = type;
    }

    public MessageKey(Message message){
        method = message.method();
        type = message.type();
    }

    public void setMethod(String method){
        this.method = method;
    }

    public String getMethod(){
        return method;
    }

    public void setType(MessageType type){
        this.type = type;
    }

    public MessageType getType(){
        return type;
    }

    @Override
    public int hashCode(){
        return method.hashCode()+type.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof MessageKey){
            return method.equals(((MessageKey) obj).method) && type.equals(((MessageKey) obj).type);
        }
        return false;
    }
}
