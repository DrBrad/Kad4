package unet.kad4.rpc.events;

import unet.kad4.messages.inter.MessageBase;
import unet.kad4.rpc.events.inter.ResponseCallback;
import unet.kad4.rpc.events.inter.MessageEvent;
import unet.kad4.utils.Node;

public class RequestEvent extends MessageEvent {

    private MessageBase response;
    private ResponseCallback callback;

    public RequestEvent(MessageBase message){
        super(message);
    }

    public RequestEvent(MessageBase message, Node node){
        super(message);
        this.node = node;
    }

    public boolean hasResponseCallback(){
        return (callback != null);
    }

    public ResponseCallback getResponseCallback(){
        return callback;
    }

    public void setResponseCallback(ResponseCallback callback){
        this.callback = callback;
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
