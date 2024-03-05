package unet.kad4.rpc;

import unet.kad4.libs.bencode.variables.BencodeObject;
import unet.kad4.messages.inter.*;
import unet.kad4.routing.inter.RoutingTable;
import unet.kad4.rpc.events.RequestEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.inter.Event;
import unet.kad4.rpc.events.inter.MessageEvent;
import unet.kad4.utils.ByteWrapper;
import unet.kad4.utils.net.AddressUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import static unet.kad4.messages.inter.MessageBase.TID_KEY;

public class RPCServer {

    public static final int MAX_ACTIVE_CALLS = 512, TID_LENGTH = 6;

    private DatagramSocket server;
    private final ConcurrentLinkedQueue<DatagramPacket> receivePool;

    private SecureRandom random;
    protected final RoutingTable routingTable;
    private ResponseTracker tracker;
    //protected final RPCReceiver receiver;
    protected final Map<MessageKey, List<Method>> receivers;
    protected final Map<MessageKey, /*Class<MessageBase>*/Constructor<MessageBase>> messages;
    //protected final Map<MessageKey, Class> encoders, decoders;

    public RPCServer(RoutingTable routingTable){
        this.routingTable = routingTable;
        tracker = new ResponseTracker();
        //receiver = new RPCReceiver(this);
        //this.dht = dht;
        receivePool = new ConcurrentLinkedQueue<>();
        receivers = new HashMap<>();
        messages = new HashMap<>();

        //encoders = new HashMap<>();
        //decoders = new HashMap<>();

        try{
            random = SecureRandom.getInstance("SHA1PRNG");
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }

        routingTable.addRestartListener(new RoutingTable.RestartListener(){
            @Override
            public void onRestart(){
                System.out.println("RESTART");
                //new BucketRefresh(RPCServer.this).run();

                //ONLY DO THIS IF WE ARE INCLUDING CACHE
                //new StaleRefresh(RPCServer.this).run();
            }
        });
    }

    public void start(int port)throws SocketException {
        //We add the packet to a pool so that we can read in a different thread so that we don't affect the RPC thread
        //without doing 2 threads we may clog up the RPC server and miss packets
        if(isRunning()){
            throw new IllegalArgumentException("Server has already started.");
        }

        server = new DatagramSocket(port);

        new Thread(new Runnable(){
            @Override
            public void run(){
                while(!server.isClosed()){
                    try{
                        DatagramPacket packet = new DatagramPacket(new byte[65535], 65535);
                        server.receive(packet);

                        if(packet != null){
                            receivePool.offer(packet);
                        }
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        new Thread(new Runnable(){
            @Override
            public void run(){
                while(!server.isClosed()){
                    if(!receivePool.isEmpty()){
                        onReceive(receivePool.poll());
                    }

                    /*
                    if(!sendPool.isEmpty()){
                        //server.send(sendPool.poll());
                    }
                    */

                    //removeStalled();
                    tracker.removeStalled();
                }
            }
        }).start();
    }

    public void stop(){
        if(!isRunning()){
            throw new IllegalArgumentException("Server is not currently running.");
        }
        server.close();
    }

    public boolean isRunning(){
        return (server != null && !server.isClosed());
    }

    public int getPort(){
        return (server != null) ? server.getLocalPort() : 0;
    }

    /*
    public void addRequestListener(RequestListener listener){
        requestListeners.add(listener);
    }

    public void removeRequestListener(RequestListener listener){
        requestListeners.remove(listener);
    }
    */

    public RoutingTable getRoutingTable(){
        //System.out.println(routingTable.getConsensusExternalAddress().getHostAddress()+"  "+routingTable.getAllNodes().size());
        return routingTable;
    }

    public void addMessage(Class<MessageBase> message)throws NoSuchMethodException {
        if(!message.isAnnotationPresent(Message.class)){
            //throw new Ann
        }
        messages.put(new MessageKey(message.getAnnotation(Message.class)), message.getDeclaredConstructor(byte[].class));
    }

    private void onReceive(DatagramPacket packet){
        if(AddressUtils.isBogon(packet.getAddress(), packet.getPort())){
            return;
        }

        //SPAM THROTTLE...

        //CATCH IF NO TID... - MESSAGE IS POINTLESS - IGNORE


        BencodeObject ben = new BencodeObject(packet.getData());

        if(!ben.containsKey(TID_KEY) || !ben.containsKey(MessageType.TYPE_KEY)){
            return;
        }

        MessageType t = MessageType.fromRPCTypeName(ben.getString(MessageType.TYPE_KEY));

        try{
            switch(t){
                case REQ_MSG: {
                        MessageKey k = new MessageKey(ben.getString(t.getRPCTypeName()), t);
                        if(!messages.containsKey(k)){
                            return;
                        }

                        MessageBase m = messages.get(k)/*.getDeclaredConstructor(byte[].class)*/.newInstance(ben.getBytes(TID_KEY));//.decode(ben);
                        m.decode(ben); //ERROR THROW - SEND ERROR MESSAGE

                        if(!receivers.containsKey(k)){
                            return;
                        }

                        RequestEvent event = new RequestEvent(m);
                        //event.setResponse(messages.get(new MessageKey(ben.getString(t.getRPCTypeName()), Type.RSP_MSG)).newInstance(ben.getBytes(TID_KEY)));

                        for(Method r : receivers.get(k)){
                            r.invoke(event); //THROW ERROR - SEND ERROR MESSAGE
                        }

                        if(event.isPreventDefault() || !event.hasResponse()){
                            return;
                        }

                        send(event.getResponse());
                    }
                    break;

                case RSP_MSG: {
                        byte[] tid = ben.getBytes(TID_KEY);
                        RequestEvent req = tracker.poll(new ByteWrapper(tid));
                        if(req == null){
                            return;
                        }

                        //VERIFY ADDRESS AND PORT ARE ACCURATE...

                        MessageKey k = new MessageKey(req.getMessage().getMethod(), t);
                        if(!messages.containsKey(k)){
                            return;
                        }

                        MessageBase m = messages.get(k)/*.getDeclaredConstructor(byte[].class)*/.newInstance(tid);//.decode(ben);
                        m.decode(ben);

                        if(!req.getMessage().getDestination().equals(m.getOrigin()) ||
                                !req.getMessage().getUID().equals(m.getUID())){
                            return;
                        }

                        ResponseEvent event = new ResponseEvent(m);
                        event.setNode(req.getNode());
                        event.setSentTime(req.getSentTime());
                        event.received();

                        for(Method r : receivers.get(k)){
                            r.invoke(event);
                        }

                        //call.getMessageCallback().onResponse(m);
                    }
                    break;

                case ERR_MSG:

                default:
                    return;
            }

        }catch(InvocationTargetException | InstantiationException | IllegalAccessException | IOException/* | MessageException*/ e){
            e.printStackTrace();
        }

        //MessageBase.Decoder.decode(ben);

        /*
        if(!ben.containsKey("t") || !ben.containsKey(Type.TYPE_KEY)){
            throw new MessageException("Required keys are missing", ErrorMessage.ErrorType.PROTOCOL);
        }

        tid = ben.getBytes("t");
        type = Type.fromRPCTypeName(ben.getString(Type.TYPE_KEY));
        */


        /*
        try{
            MessageDecoder d = new MessageDecoder(packet.getData());

            switch(d.getType()){
                case REQ_MSG: {
                    try{


                        //MessageBase m = d.decodeRequest();

                        /*
                        if(events.containsKey()){

                        }*./



                        m.setOrigin(packet.getAddress(), packet.getPort());

                        routingTable.insert(new Node(m.getUID(), m.getOrigin()));
                        System.out.println("SEEN RQ: "+new Node(m.getUID(), m.getOrigin()));



                        //for( event : events.keySet()){
                        //    event.
                        //}
                        receiver.onRequest(m);

                    }catch(MessageException e){
                        ErrorMessage m = new ErrorMessage(d.getTransactionID());
                        m.setErrorType(e.getErrorType());
                        m.setDestination(packet.getAddress(), packet.getPort());
                        m.setPublic(packet.getAddress(), packet.getPort());
                        send(new RPCCall(m));
                        //e.printStackTrace();
                    }
                }
                break;

                case RSP_MSG: {
                        ByteWrapper tid = new ByteWrapper(d.getTransactionID());

                        if(!callsOrder.contains(tid)){
                            return;
                        }

                        RPCRequestCall call = calls.get(tid);
                        callsOrder.remove(tid);
                        calls.remove(tid);

                        //ENSURE RESPONSE IS ADDRESS IS ACCURATE...
                        if(!packet.getAddress().equals(call.getMessage().getDestinationAddress()) ||
                                packet.getPort() != call.getMessage().getDestinationPort()){
                            return;
                        }

                    //try{
                        MessageBase m = d.decodeResponse(/*call.getMessage().getMethod()*./);
                        m.setOrigin(packet.getAddress(), packet.getPort());

                        if(m.getPublic() != null){
                            routingTable.updatePublicIPConsensus(m.getOriginAddress(), m.getPublicAddress());
                        }

                        call.getMessageCallback().onResponse(m);

                    //}catch(MessageException e){
                    //    call.getMessageCallback().onException(e);
                    //}
                }
                break;

                case ERR_MSG: {
                        ByteWrapper tid = new ByteWrapper(d.getTransactionID());

                        if(!callsOrder.contains(tid)){
                            return;
                        }

                        RPCRequestCall call = calls.get(tid);
                        callsOrder.remove(tid);
                        calls.remove(tid);

                        //ENSURE RESPONSE IS ADDRESS IS ACCURATE...
                        if(!packet.getAddress().equals(call.getMessage().getDestinationAddress()) ||
                                packet.getPort() != call.getMessage().getDestinationPort()){
                            return;
                        }

                    //try{
                        ErrorMessage m = d.decodeError();
                        m.setOrigin(packet.getAddress(), packet.getPort());

                        if(m.getPublic() != null){
                            routingTable.updatePublicIPConsensus(m.getOriginAddress(), m.getPublicAddress());
                        }

                        call.getMessageCallback().onErrorResponse(m);

                    //}catch(MessageException e){
                    //    call.getMessageCallback().onException(e);
                    //}
                }
                break;
            }
        }catch(MessageException e){
            //WE CANT TRUST THE MESSAGE - WE SHOULDN'T ACCEPT IF NO TID OR MESSAGE TYPE IS DEFINED
            //RESPONSE MALFORMED SHOULD BE IGNORED... - MAYBE WE SAVE TO ROUTING TABLE...?
            e.printStackTrace();
        }
        */
    }

    public void send(MessageEvent event)throws IOException {
        event.getMessage().setUID(routingTable.getDerivedUID());

        if(event instanceof RequestEvent){
            byte[] tid = generateTransactionID(); //TRY UP TO 5 TIMES TO GENERATE RANDOM - NOT WITHIN CALLS...
            event.getMessage().setTransactionID(tid);
            tracker.add(new ByteWrapper(tid), (RequestEvent) event);
        }

        //try{
            send(event.getMessage());
            event.sent();
        //}catch(IOException e){
            //FAILED
        //}
            /*
            switch(event.getMessage().getType()){
                case REQ_MSG:
                    byte[] tid = generateTransactionID(); //TRY UP TO 5 TIMES TO GENERATE RANDOM - NOT WITHIN CALLS...
                    event.getMessage().setTransactionID(tid);

                    //RequestEvent event = new RequestEvent(message);
                    event.sent();
                    tracker.add(new ByteWrapper(tid), (RequestEvent) event);
                    break;
            }
            */
    }

    private void send(MessageBase message)throws IOException {
        if(message.getDestination() == null){
            throw new IllegalArgumentException("Message destination set to null");
        }

        byte[] data = message.encode().encode();
        DatagramPacket packet = new DatagramPacket(data, 0, data.length, message.getDestination());

        server.send(packet);
    }

    /*
    public void send(MessageBase message){
        switch(message.getType()){
            case REQ_MSG:
                throw new IllegalArgumentException("Request messages require a callback");

            case RSP_MSG:
            case ERR_MSG:

                break;
        }
    }

    public void send(MessageBase message, MessageCallback callback){
        switch(message.getType()){
            case REQ_MSG:

            case RSP_MSG:
            case ERR_MSG:
                throw new IllegalArgumentException("Only request messages can have a callback");
        }

        try{
            if(message.getDestination() == null){
                throw new IllegalArgumentException("Message destination set to null");
            }

            message.setUID(routingTable.getDerivedUID());

            switch(message.getType()){
                case REQ_MSG:
                    byte[] tid = generateTransactionID(); //TRY UP TO 5 TIMES TO GENERATE RANDOM - NOT WITHIN CALLS...
                    message.setTransactionID(tid);

                    RequestEvent event = new RequestEvent(message);
                    event.sent();
                    tracker.add(new ByteWrapper(tid), event);
                    break;
            }

            byte[] data = message.encode().encode();
            DatagramPacket packet = new DatagramPacket(data, 0, data.length, message.getDestination());

            server.send(packet);
            /*
            call.getMessage().setUID(routingTable.getDerivedUID());

            switch(call.getMessage().getType()){
                case REQ_MSG:
                    byte[] tid = generateTransactionID(); //TRY UP TO 5 TIMES TO GENERATE RANDOM - NOT WITHIN CALLS...
                    call.getMessage().setTransactionID(tid);
                    ((RPCRequestCall) call).sent();
                    ByteWrapper wrapper = new ByteWrapper(tid);
                    callsOrder.add(wrapper);
                    calls.put(wrapper, (RPCRequestCall) call);
                    break;
            }

            byte[] data = call.getMessage().encode();
            DatagramPacket packet = new DatagramPacket(data, 0, data.length, call.getMessage().getDestination());

            server.send(packet);
            *./

        }catch(IOException e){
            e.printStackTrace();
        }
    }
    */

    /*
    private void removeStalled(){
        long now = System.currentTimeMillis();
        //for(int i = calls.size()-1; i > -1; i--){
        //for(ByteWrapper tid : calls.keySet()){
        //for(int i = 0; i < calls.size(); i++){
        for(ByteWrapper tid : callsOrder){
            Call call = calls.get(tid);
            if(!call.isStalled(now)){
                break;
            }

            callsOrder.remove(tid);
            calls.remove(tid);
            call.getMessageCallback().onStalled();

            //call.getMessageCallback().onResponse(call.getMessage());
        }
    }
    */

    //DONT INIT EVERY TIME...
    private byte[] generateTransactionID(){
        byte[] tid = new byte[TID_LENGTH];
        random.nextBytes(tid);
        return tid;
    }
}
