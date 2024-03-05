package unet.kad4.rpc.events.inter;

import unet.kad4.messages.ErrorMessage;
import unet.kad4.messages.inter.MessageBase;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.StalledEvent;

public abstract class ResponseCallback {

    public abstract void onResponse(ResponseEvent event);

    public void onErrorResponse(ResponseEvent event){
    }

    //public void onException(MessageException exception){
    //}

    public void onStalled(StalledEvent event){
    }
}
