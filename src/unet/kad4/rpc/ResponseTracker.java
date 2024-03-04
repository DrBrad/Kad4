package unet.kad4.rpc;

import unet.kad4.rpc.events.RequestEvent;
import unet.kad4.utils.ByteWrapper;

import java.util.LinkedHashMap;

public class ResponseTracker {

    public static final int MAX_ACTIVE_CALLS = 512;

    public static final long STALLED_TIME = 60000;
    private final LinkedHashMap<ByteWrapper, RequestEvent> calls;
    //private final ConcurrentHashMap<ByteWrapper, RequestEvent> calls;
    //private final ConcurrentLinkedQueue<ByteWrapper> callsOrder;

    public ResponseTracker(){
        calls = new LinkedHashMap<>(MAX_ACTIVE_CALLS);
        //calls = new ConcurrentHashMap<>(MAX_ACTIVE_CALLS);
        //callsOrder = new ConcurrentLinkedQueue<>();
    }

    public synchronized void add(ByteWrapper tid, RequestEvent event){
        calls.put(tid, event);
    }

    public synchronized RequestEvent get(ByteWrapper tid){
        return calls.get(tid);
    }

    public synchronized boolean contains(ByteWrapper tid){
        return calls.containsKey(tid);
    }

    public synchronized void remove(ByteWrapper tid){
        calls.remove(tid);
    }

    public synchronized RequestEvent poll(ByteWrapper tid){
        RequestEvent event = calls.get(tid);
        calls.remove(tid);
        return event;
    }

    public synchronized void removeStalled(){
        long now = System.currentTimeMillis();

        for(ByteWrapper tid : calls.keySet()){
            RequestEvent event = calls.get(tid);
            if(!event.isStalled(now)){
                break;
            }

            calls.remove(tid);
            //call.getMessageCallback().onStalled();
        }
        /*
        for(ByteWrapper tid : callsOrder){
            RPCCall call = calls.get(tid);
            if(!call.isStalled(now)){
                break;
            }

            callsOrder.remove(tid);
            calls.remove(tid);
            call.getMessageCallback().onStalled();

            //call.getMessageCallback().onResponse(call.getMessage());
        }
        */
    }
}
