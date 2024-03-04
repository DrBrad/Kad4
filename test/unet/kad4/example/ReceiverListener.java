package unet.kad4.example;

import unet.kad4.messages.PingResponse;
import unet.kad4.messages.inter.MessageType;
import unet.kad4.rpc.events.RequestEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.inter.EventHandler;

public class ReceiverListener {

    @EventHandler(method = "ping", type = MessageType.REQ_MSG)
    public void onReceive(RequestEvent event){
        //event.preventDefault();
        PingResponse response = new PingResponse(event.getMessage().getTransactionID());
        response.setDestination(event.getMessage().getOrigin());
        response.setPublic(event.getMessage().getOrigin());

        event.setResponse(response);
    }

    @EventHandler(method = "ping", type = MessageType.RSP_MSG)
    public void onResponse(ResponseEvent event){

    }
}
