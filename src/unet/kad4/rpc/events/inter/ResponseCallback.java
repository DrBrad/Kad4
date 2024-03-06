package unet.kad4.rpc.events.inter;

import unet.kad4.rpc.events.ErrorResponseEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.StalledEvent;

public abstract class ResponseCallback {

    public abstract void onResponse(ResponseEvent event);

    public void onErrorResponse(ErrorResponseEvent event){
    }

    //public void onException(MessageException exception){
    //}

    public void onStalled(StalledEvent event){
    }
}
