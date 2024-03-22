package unet.kad4;

import unet.bencode.variables.BencodeObject;
import unet.kad4.messages.ErrorResponse;
import unet.kad4.messages.inter.*;
import unet.kad4.rpc.Call;
import unet.kad4.rpc.ResponseTracker;
import unet.kad4.rpc.events.ErrorResponseEvent;
import unet.kad4.rpc.events.RequestEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.inter.ResponseCallback;
import unet.kad4.utils.ByteWrapper;
import unet.kad4.utils.Node;
import unet.kad4.utils.ReflectMethod;
import unet.kad4.utils.net.AddressUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static unet.kad4.messages.inter.MessageBase.TID_KEY;

public class Server {

    public static final int TID_LENGTH = 6;

    private DatagramSocket server;

    private SecureRandom random;
    protected final Kademlia kademlia;
    private ResponseTracker tracker;

    public Server(Kademlia kademlia){
        this.kademlia = kademlia;
        tracker = new ResponseTracker();

        try{
            random = SecureRandom.getInstance("SHA1PRNG");
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }
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
                            new Thread(new Runnable(){
                                @Override
                                public void run(){
                                    onReceive(packet);
                                    tracker.removeStalled();
                                }
                            }).start();
                        }
                    }catch(IOException e){
                        e.printStackTrace();
                    }
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

                        MethodMessageBase m = (MethodMessageBase) kademlia.messages.get(k)/*.getDeclaredConstructor(byte[].class)*/.newInstance(ben.getBytes(TID_KEY));//.decode(ben);
                        m.decode(ben); //ERROR THROW - SEND ERROR MESSAGE
                        m.setOrigin(packet.getAddress(), packet.getPort());


                    System.err.println("RECEIVED MESSAGE  "+k.getMethod()+"  "+k.getType());

                        if(!kademlia.requestMapping.containsKey(m.getMethod())){
                            return;
                        }

                        Node node = new Node(m.getUID(), m.getOrigin());
                        kademlia.routingTable.insert(node);
                        System.out.println("SEEN REQ "+node);

                        RequestEvent event = new RequestEvent(m, node);
                        event.received();
                        //event.setResponse(messages.get(new MessageKey(ben.getString(t.getRPCTypeName()), Type.RSP_MSG)).newInstance(ben.getBytes(TID_KEY)));

                        for(ReflectMethod r : kademlia.requestMapping.get(m.getMethod()/*new EventKey(m.getMethod(), m.getType())*/)){
                            r.getMethod().invoke(r.getInstance(), event); //THROW ERROR - SEND ERROR MESSAGE
                        }

                        if(event.isPreventDefault() || !event.hasResponse()){
                            return;
                        }

                        send(event.getResponse());

                        if(!kademlia.getRefreshHandler().isRunning()){
                            kademlia.getRefreshHandler().start();
                        }
                    }
                    break;

                case RSP_MSG: {
                        byte[] tid = ben.getBytes(TID_KEY);
                        Call call = tracker.poll(new ByteWrapper(tid));
                        if(call == null){
                            return;
                        }

                        MessageKey k = new MessageKey(((MethodMessageBase) call.getMessage()).getMethod(), t);
                        if(!kademlia.messages.containsKey(k)){
                            return;
                        }

                        MethodMessageBase m = (MethodMessageBase) kademlia.messages.get(k)/*.getDeclaredConstructor(byte[].class)*/.newInstance(tid);//.decode(ben);
                        m.decode(ben);
                        m.setOrigin(packet.getAddress(), packet.getPort());

                        if(m.getPublic() != null){
                            kademlia.getRoutingTable().updatePublicIPConsensus(m.getOriginAddress(), m.getPublicAddress());
                        }

                        //!req.getMessage().getUID().equals(m.getUID()) - THAT WOULDNT MATCH UP...
                        if(!call.getMessage().getDestination().equals(m.getOrigin())){
                            return;
                        }

                        ResponseEvent event;

                        if(call.hasNode()){
                            if(!call.getNode().getUID().equals(m.getUID())){
                                return;
                            }
                            event = new ResponseEvent(m, call.getNode());

                        }else{
                            event = new ResponseEvent(m, new Node(m.getUID(), m.getOrigin()));
                        }

                        event.received();
                        event.setSentTime(call.getSentTime());
                        event.setRequest(call.getMessage());

                        if(call.hasResponseCallback()){
                            call.getResponseCallback().onResponse(event);
                        }
                    }
                    break;

                case ERR_MSG: {
                        byte[] tid = ben.getBytes(TID_KEY);
                        Call call = tracker.poll(new ByteWrapper(tid));
                        if(call == null){
                            return;
                        }

                        ErrorResponse m = new ErrorResponse(tid);
                        m.decode(ben);
                        m.setOrigin(packet.getAddress(), packet.getPort());

                        if(m.getPublic() != null){
                            kademlia.getRoutingTable().updatePublicIPConsensus(m.getOriginAddress(), m.getPublicAddress());
                        }

                        //!req.getMessage().getUID().equals(m.getUID()) - THAT WOULDNT MATCH UP...
                        if(!call.getMessage().getDestination().equals(m.getOrigin())){
                            return;
                        }

                        ErrorResponseEvent event;// = new ErrorResponseEvent(m);
                        //

                        if(call.hasNode()){
                            if(!call.getNode().getUID().equals(m.getUID())){
                                return;
                            }
                            event = new ErrorResponseEvent(m, call.getNode());

                        }else{
                            event = new ErrorResponseEvent(m, new Node(m.getUID(), m.getOrigin()));
                        }

                        event.received();
                        event.setSentTime(call.getSentTime());
                        event.setRequest(call.getMessage());

                        if(call.hasResponseCallback()){
                            call.getResponseCallback().onErrorResponse(event);
                        }
                    }
                    break;
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

    public void send(MessageBase message)throws IOException {
        if(message.getDestination() == null){
            throw new IllegalArgumentException("Message destination set to null");
        }

        if(AddressUtils.isBogon(message.getDestination())){
            throw new IllegalArgumentException("Message destination set to bogon");
        }

        message.setUID(kademlia.routingTable.getDerivedUID());

        byte[] data = message.encode().encode();
        server.send(new DatagramPacket(data, 0, data.length, message.getDestination()));
    }

    public void send(MessageBase message, ResponseCallback callback)throws IOException {
        if(message.getType() != MessageType.REQ_MSG){
            send(message);
        }

        byte[] tid = generateTransactionID();
        message.setTransactionID(tid);
        tracker.add(new ByteWrapper(tid), new Call(message, callback));
        send(message);
    }

    public void send(MessageBase message, Node node, ResponseCallback callback)throws IOException {
        if(message.getType() != MessageType.REQ_MSG){
            send(message);
        }

        byte[] tid = generateTransactionID();
        message.setTransactionID(tid);
        tracker.add(new ByteWrapper(tid), new Call(message, node, callback));
        send(message);
    }

    //DONT INIT EVERY TIME...
    private byte[] generateTransactionID(){
        byte[] tid = new byte[TID_LENGTH];
        random.nextBytes(tid);
        return tid;
    }
}
