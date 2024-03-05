package unet.kad4.rpc;

import unet.kad4.messages.PingResponse;
import unet.kad4.messages.inter.MessageType;
import unet.kad4.rpc.events.RequestEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.inter.EventHandler;

public class KEventListener {

    @EventHandler(method = "ping", type = MessageType.REQ_MSG)
    public void onRequest(RequestEvent event){
        if(event.isPreventDefault()){
            return;
        }

        PingResponse response = new PingResponse(event.getMessage().getTransactionID());
        response.setDestination(event.getMessage().getOrigin());
        response.setPublic(event.getMessage().getOrigin());
        event.setResponse(response);
    }

    @EventHandler
    public void onResponse(ResponseEvent event){
        if(event.hasNode()){

        }
    }
}
