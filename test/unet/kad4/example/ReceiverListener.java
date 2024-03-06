package unet.kad4.example;

import unet.kad4.messages.PingResponse;
import unet.kad4.messages.inter.MessageType;
import unet.kad4.rpc.events.RequestEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.inter.RequestMapping;

public class ReceiverListener {

    @RequestMapping("ping")
    public void onReceive(RequestEvent event){
        //event.preventDefault();
        PingResponse response = new PingResponse(event.getMessage().getTransactionID());
        response.setDestination(event.getMessage().getOrigin());
        response.setPublic(event.getMessage().getOrigin());

        event.setResponse(response);
    }
}
