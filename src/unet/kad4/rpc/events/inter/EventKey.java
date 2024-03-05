package unet.kad4.rpc.events.inter;

import unet.kad4.messages.inter.MessageKey;
import unet.kad4.messages.inter.MessageType;

public class EventKey {

    private String method;
    private MessageType type;
    private Priority priority = Priority.DEFAULT;

    public EventKey(String method, MessageType type){
        this.method = method;
        this.type = type;
    }

    public EventKey(String method, MessageType type, Priority priority){
        this.method = method;
        this.type = type;
        this.priority = priority;
    }

    public EventKey(EventHandler eventHandler){
        method = eventHandler.method();
        type = eventHandler.type();
        priority = eventHandler.priority();
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

    public void setPriority(Priority priority){
        this.priority = priority;
    }

    public Priority getPriority(){
        return priority;
    }

    @Override
    public int hashCode(){
        return method.hashCode()+type.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof EventKey){
            return method.equals(((EventKey) obj).method) && type.equals(((EventKey) obj).type);
        }
        return false;
    }
}