package unet.kad4.rpc.events.inter;

public class Event {

    protected boolean preventDefault;

    public boolean isPreventDefault(){
        return preventDefault;
    }

    public void preventDefault(){
        preventDefault = true;
    }
}
