package unet.kad4;

import unet.kad4.messages.FindNodeRequest;
import unet.kad4.messages.FindNodeResponse;
import unet.kad4.messages.PingRequest;
import unet.kad4.messages.PingResponse;
import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageBase;
import unet.kad4.messages.inter.MessageKey;
import unet.kad4.refresh.tasks.BucketRefreshTask;
import unet.kad4.refresh.tasks.StaleRefreshTask;
import unet.kad4.routing.BucketTypes;
import unet.kad4.routing.inter.RoutingTable;
import unet.kad4.rpc.EventListener;
import unet.kad4.rpc.KEventListener;
import unet.kad4.refresh.RefreshHandler;
import unet.kad4.rpc.events.RequestEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.inter.EventKey;
import unet.kad4.rpc.events.inter.MessageEvent;
import unet.kad4.rpc.events.inter.EventHandler;
import unet.kad4.rpc.events.inter.ResponseCallback;
import unet.kad4.utils.Node;
import unet.kad4.utils.ReflectMethod;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Kademlia {

    protected RoutingTable routingTable;
    private Server server;
    private RefreshHandler refresh;
    //private DHT dht;

    //ALLOW DHT SPECIFICATION
    protected Map<EventKey, List<ReflectMethod>> eventListeners;
    protected Map<MessageKey, Constructor<?>> messages;

    public Kademlia(){
        this(BucketTypes.KADEMLIA.getRoutingTable());
    }

    /*
    public Kademlia(){
        this(BucketTypes.KADEMLIA.getRoutingTable());
    }
    */

    public Kademlia(String bucketType){
        this(BucketTypes.fromString(bucketType).getRoutingTable());
    }

    public Kademlia(RoutingTable routingTable){
        this.routingTable = routingTable;
        System.out.println("Starting with bucket type: "+routingTable.getClass().getSimpleName());
        server = new Server(this);
        refresh = new RefreshHandler(this);
        //refresh.addOperation(new BucketRefresh(server));
        //refresh.addOperation(new StaleRefresh(server));
        //new RPCHandler()
        //dht = new KDHT(server);
        eventListeners = new HashMap<>();
        messages = new HashMap<>();

        try{
            registerEventListener(KEventListener.class);

            registerMessage(PingRequest.class);
            registerMessage(PingResponse.class);
            registerMessage(FindNodeRequest.class);
            registerMessage(FindNodeResponse.class);

            refresh.addOperation(BucketRefreshTask.class);
            refresh.addOperation(StaleRefreshTask.class);

        }catch(NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
    }

    public void registerEventListener(Class<?> c)throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if(!c.getSuperclass().equals(EventListener.class)){
            throw new IllegalArgumentException("Class '"+c.getSimpleName()+"' isn't a super of 'EventListener'");
        }

        EventListener e = (EventListener) c.getDeclaredConstructor(Kademlia.class).newInstance(this);

        for(Method method : c.getDeclaredMethods()){
            if(method.isAnnotationPresent(EventHandler.class)){
                Parameter[] parameters = method.getParameters();

                if(parameters.length != 1){
                    continue;
                }

                if(parameters[0].getType().getSuperclass().equals(MessageEvent.class)){
                    method.setAccessible(true);

                    EventKey key = new EventKey(method.getAnnotation(EventHandler.class));

                    System.out.println("Registered "+method.getName()+" event handler");

                    if(eventListeners.containsKey(key)){
                        eventListeners.get(key).add(new ReflectMethod(e, method));
                        continue;
                    }

                    List<ReflectMethod> m = new ArrayList<>();
                    m.add(new ReflectMethod(e, method));
                    eventListeners.put(key, m);
                }
            }
        }
    }

    public void registerMessage(Class<?> c)throws NoSuchMethodException {
        if(!c.getSuperclass().equals(MessageBase.class)){
            throw new IllegalArgumentException("Class doesn't extend 'MessageBase' class");
        }

        if(!c.isAnnotationPresent(Message.class)){
            throw new IllegalArgumentException("Class is missing '@Message' annotation");
        }

        messages.put(new MessageKey(c.getAnnotation(Message.class)), c.getDeclaredConstructor(byte[].class));
        System.out.println("Registered "+c.getSimpleName()+" message");
    }

    public Server getServer(){
        return server;
    }

    public RoutingTable getRoutingTable(){
        return routingTable;
    }

    public RefreshHandler getRefreshHandler(){
        return refresh;
    }

    public void join(int localPort, InetAddress address, int port)throws IOException {
        join(localPort, new InetSocketAddress(address, port));
    }

    public void join(int localPort, Node node)throws IOException {
        join(localPort, node.getAddress());
        /*
        if(!server.isRunning()){
            server.start(localPort);
        }

        FindNodeRequest request = new FindNodeRequest();
        request.setDestination(node.getAddress());
        request.setTarget(routingTable.getDerivedUID());

        RequestEvent event = new RequestEvent(request);
        event.setResponseCallback(new ResponseCallback(){
            @Override
            public void onResponse(ResponseEvent event){
                routingTable.insert(node);

                System.out.println("INSERTED NODE");
            }
        });

        server.send(event); //WHAT ABOUT REFRESH... WE NEED A CALLBACK...
        */
    }

    public void join(int localPort, InetSocketAddress address)throws IOException {
        //bind(localPort);
        if(!server.isRunning()){
            server.start(localPort);
        }

        FindNodeRequest request = new FindNodeRequest();
        request.setDestination(address);
        request.setTarget(routingTable.getDerivedUID());

        RequestEvent event = new RequestEvent(request);
        event.setResponseCallback(new ResponseCallback(){
            @Override
            public void onResponse(ResponseEvent event){
                routingTable.insert(event.getNode());

                FindNodeResponse response = (FindNodeResponse) event.getMessage();

                if(response.hasNodes()){
                    List<Node> nodes = response.getAllNodes();

                    long now = System.currentTimeMillis();
                    for(Node n : nodes){
                        if(!n.hasSecureID() || n.hasQueried(now)){
                            System.out.println("SKIPPING "+now+"  "+n.getLastSeen()+"  "+n);
                            continue;
                        }

                        //System.out.println("PINGING "+n);

                        PingRequest req = new PingRequest();
                        req.setDestination(n.getAddress());
                        try{
                            getServer().send(new RequestEvent(req, n));
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }

                if(!refresh.isRunning()){
                    refresh.start();
                }
            }
        });

        server.send(event); //WHAT ABOUT REFRESH... WE NEED A CALLBACK...
        //dht.join(address);

        //new JoinOperation(server, refresh, address).run();
    }

    public void bind()throws SocketException {
        bind(0);
    }

    public void bind(int port)throws SocketException {
        if(!server.isRunning()){
            server.start(port);
        }

        if(!refresh.isRunning()){
            refresh.start();
        }

        /*
        if(dht != null){
            dht.start();
            return;
        }
        dht = new KDHT(server);
        dht.start();
        */
    }

    /*
    public RefreshHandler getRefreshHandler(){
        return refresh;
    }

    public UID getUID(){
        return server.getRoutingTable().getDerivedUID();
    }

    public InetAddress getConsensusAddress(){
        return server.getRoutingTable().getConsensusExternalAddress();
    }

    public int getRouterSize(){
        return server.getRoutingTable().getAllNodes().size();
    }
    */

    /*
    public void setDHT(Class<?> c){
        if(DHT.class.isAssignableFrom(c)){
            try{
                Constructor<?> constructor = c.getConstructor(RPCServer.class);
                DHT dht = (DHT) constructor.newInstance(server);

                if(this.dht != null){
                    this.dht.stop();
                }
                dht.start();

            }catch(NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e){
                e.printStackTrace();
            }
        }
    }

    public DHT getDHT(){
        return dht;
    }
    */

    public void stop(){
        server.stop();
        refresh.stop();
        /*
        if(dht != null){
            dht.stop();
        }
        */
    }
}
