package unet.kad4;

import unet.kad4.Kademlia;
import unet.kad4.libs.bencode.variables.BencodeObject;
import unet.kad4.messages.inter.*;
import unet.kad4.routing.inter.RoutingTable;
import unet.kad4.rpc.ResponseTracker;
import unet.kad4.rpc.events.RequestEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.inter.EventKey;
import unet.kad4.rpc.events.inter.MessageEvent;
import unet.kad4.utils.ByteWrapper;
import unet.kad4.utils.Node;
import unet.kad4.utils.ReflectMethod;
import unet.kad4.utils.UID;
import unet.kad4.utils.net.AddressUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentLinkedQueue;

import static unet.kad4.messages.inter.MessageBase.TID_KEY;

public class Server {

    public static final int MAX_ACTIVE_CALLS = 512, TID_LENGTH = 6;

    private DatagramSocket server;
    private final ConcurrentLinkedQueue<DatagramPacket> receivePool;

    private SecureRandom random;
    protected final Kademlia kademlia;
    private ResponseTracker tracker;
    //protected final RPCReceiver receiver;
    //protected final Map<MessageKey, List<Method>> receivers;
    //protected final Map<MessageKey, /*Class<MessageBase>*/Constructor<MessageBase>> messages;
    //protected final Map<MessageKey, Class> encoders, decoders;

    public Server(Kademlia kademlia){
        this.kademlia = kademlia;
        tracker = new ResponseTracker();
        //receiver = new RPCReceiver(this);
        //this.dht = dht;
        receivePool = new ConcurrentLinkedQueue<>();


        //encoders = new HashMap<>();
        //decoders = new HashMap<>();

        try{
            random = SecureRandom.getInstance("SHA1PRNG");
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }

        kademlia.routingTable.addRestartListener(new RoutingTable.RestartListener(){
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
                    //tracker.removeStalled();
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
                        if(!kademlia.messages.containsKey(k)){
                            return;
                        }

                        MessageBase m = (MessageBase) kademlia.messages.get(k)/*.getDeclaredConstructor(byte[].class)*/.newInstance(ben.getBytes(TID_KEY));//.decode(ben);
                        m.decode(ben); //ERROR THROW - SEND ERROR MESSAGE
                        m.setOrigin(packet.getAddress(), packet.getPort());

                        if(!kademlia.eventListeners.containsKey(k)){
                            return;
                        }

                        Node node = new Node(m.getUID(), m.getOrigin());
                        kademlia.routingTable.insert(node);
                        System.out.println("SEEN REQ "+node);

                        RequestEvent event = new RequestEvent(m);
                        event.received();
                        event.setNode(node);
                        //event.setResponse(messages.get(new MessageKey(ben.getString(t.getRPCTypeName()), Type.RSP_MSG)).newInstance(ben.getBytes(TID_KEY)));

                        for(ReflectMethod r : kademlia.eventListeners.get(new EventKey(m.getMethod(), m.getType()))){
                            r.getMethod().invoke(r.getInstance(), event); //THROW ERROR - SEND ERROR MESSAGE
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
                        if(!kademlia.messages.containsKey(k)){
                            return;
                        }

                        MessageBase m = (MessageBase) kademlia.messages.get(k)/*.getDeclaredConstructor(byte[].class)*/.newInstance(tid);//.decode(ben);
                        m.decode(ben);
                        m.setOrigin(packet.getAddress(), packet.getPort());

                        if(m.getPublic() != null){
                            kademlia.getRoutingTable().updatePublicIPConsensus(m.getOriginAddress(), m.getPublicAddress());
                        }

                        //!req.getMessage().getUID().equals(m.getUID()) - THAT WOULDNT MATCH UP...
                        if(!req.getMessage().getDestination().equals(m.getOrigin())){
                            return;
                        }

                        ResponseEvent event = new ResponseEvent(m);
                        event.received();

                        if(req.hasNode()){
                            if(!req.getNode().getUID().equals(m.getUID())){
                                return;
                            }
                            event.setNode(req.getNode());
                        }else{
                            event.setNode(new Node(m.getUID(), m.getOrigin()));
                        }

                        event.setNode(new Node(m.getUID(), m.getOrigin()));
                        event.setSentTime(req.getSentTime());
                        event.setRequest(req.getMessage());

                        //System.out.println(k.getMethod()+"  "+k.getType());

                        for(ReflectMethod r : kademlia.eventListeners.get(new EventKey(m.getMethod(), m.getType()))){
                            //System.out.println(r.getMethod().getName()+"  "+r.getMethod().getParameters()[0].getType().getSimpleName());
                            r.getMethod().invoke(r.getInstance(), event);
                        }

                        /*
                        if(kademlia.eventListeners.containsKey(new EventKey("*"))){

                        }
                        */

                        //PROBABLY JUST PREVENT THE CALLBACK...
                        if(event.isPreventDefault()){
                            return;
                        }

                        if(req.hasResponseCallback()){
                            req.getResponseCallback().onResponse(event);
                        }

                        //call.getMessageCallback().onResponse(m);
                    }
                    break;

                case ERR_MSG:

                default:
                    return;
            }

        }catch(IllegalArgumentException | InvocationTargetException | InstantiationException | IllegalAccessException | IOException/* | MessageException*/ e){
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

        message.setUID(kademlia.routingTable.getDerivedUID());

        byte[] data = message.encode().encode();
        server.send(new DatagramPacket(data, 0, data.length, message.getDestination()));
    }

    //DONT INIT EVERY TIME...
    private byte[] generateTransactionID(){
        byte[] tid = new byte[TID_LENGTH];
        random.nextBytes(tid);
        return tid;
    }
}
