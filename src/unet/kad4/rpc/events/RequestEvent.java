package unet.kad4.rpc.events;

import unet.kad4.messages.inter.MessageBase;
import unet.kad4.rpc.events.inter.Event;
import unet.kad4.rpc.events.inter.MessageEvent;

public class RequestEvent extends MessageEvent {

    private MessageBase response;

    public RequestEvent(MessageBase message){
        super(message);
    }

    public boolean hasResponse(){
        return (response != null);
    }

    public MessageBase getResponse(){
        return response;
    }

    public void setResponse(MessageBase message){
        response = message;
    }

    //public MessageBase getResponse(){
    //    return response;
    //}

    /*
    public RequestEvent(MessageBase message, MessageCallback callback){
        this.callback = callback;
    }
    */
}
