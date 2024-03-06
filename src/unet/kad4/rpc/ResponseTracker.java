package unet.kad4.rpc;

import unet.kad4.rpc.events.RequestEvent;
import unet.kad4.rpc.events.StalledEvent;
import unet.kad4.utils.ByteWrapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ResponseTracker {

    public static final int MAX_ACTIVE_CALLS = 512;

    public static final long STALLED_TIME = 60000;
    private final LinkedHashMap<ByteWrapper, Call> calls;
    //private final ConcurrentHashMap<ByteWrapper, RequestEvent> calls;
    //private final ConcurrentLinkedQueue<ByteWrapper> callsOrder;

    public ResponseTracker(){
        calls = new LinkedHashMap<>(MAX_ACTIVE_CALLS);
        //calls = new ConcurrentHashMap<>(MAX_ACTIVE_CALLS);
        //callsOrder = new ConcurrentLinkedQueue<>();
    }

    public synchronized void add(ByteWrapper tid, Call call){
        calls.put(tid, call);
    }

    public synchronized Call get(ByteWrapper tid){
        return calls.get(tid);
    }

    public synchronized boolean contains(ByteWrapper tid){
        return calls.containsKey(tid);
    }

    public synchronized void remove(ByteWrapper tid){
        calls.remove(tid);
    }

    public synchronized Call poll(ByteWrapper tid){
        Call call = calls.get(tid);
        calls.remove(tid);
        return call;
    }

    public synchronized void removeStalled(){
        long now = System.currentTimeMillis();

        List<ByteWrapper> stalled = new ArrayList<>();

        for(ByteWrapper tid : calls.keySet()){
            if(!calls.get(tid).isStalled(now)){
                break;
            }

            stalled.add(tid);
        }

        for(ByteWrapper tid : stalled){
            Call call = calls.get(tid);
            calls.remove(tid);
            System.err.println("STALLED "+((call.hasNode()) ? call.getNode() : ""));

            if(call.hasResponseCallback()){
                StalledEvent e = new StalledEvent(call.getMessage());
                //e.setSentTime(call.getSentTime());
                call.getResponseCallback().onStalled(e);
            }
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
