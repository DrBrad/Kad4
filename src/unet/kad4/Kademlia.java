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
import unet.kad4.rpc.PingResponseListener;
import unet.kad4.rpc.RequestListener;
import unet.kad4.rpc.KRequestListener;
import unet.kad4.refresh.RefreshHandler;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.inter.MessageEvent;
import unet.kad4.rpc.events.inter.PriorityComparator;
import unet.kad4.rpc.events.inter.RequestMapping;
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
import java.util.*;

public class Kademlia {

    protected RoutingTable routingTable;
    private Server server;
    private RefreshHandler refresh;
    //private DHT dht;

    //ALLOW DHT SPECIFICATION
    protected Map<String, List<ReflectMethod>> requestMapping;
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
        BucketRefreshTask bucketRefreshTask = new BucketRefreshTask();

        routingTable.addRestartListener(new RoutingTable.RestartListener(){
            @Override
            public void onRestart(){
                bucketRefreshTask.execute();
            }
        });

        requestMapping = new HashMap<>();
        messages = new HashMap<>();

        try{
            registerRequestListener(new KRequestListener());

            registerMessage(PingRequest.class);
            registerMessage(PingResponse.class);
            registerMessage(FindNodeRequest.class);
            registerMessage(FindNodeResponse.class);

            refresh.addOperation(bucketRefreshTask);
            refresh.addOperation(new StaleRefreshTask());

        }catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
    }

    public void registerRequestListener(RequestListener listener)throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method setKademlia = RequestListener.class.getDeclaredMethod("setKademlia", Kademlia.class);
        setKademlia.setAccessible(true);
        setKademlia.invoke(listener, this);

        for(Method method : listener.getClass().getDeclaredMethods()){
            if(method.isAnnotationPresent(RequestMapping.class)){
                Parameter[] parameters = method.getParameters();

                if(parameters.length != 1){
                    continue;
                }

                if(parameters[0].getType().getSuperclass().equals(MessageEvent.class)){
                    method.setAccessible(true);

                    //EventKey key = new EventKey(method.getAnnotation(RequestMapping.class));
                    String key = method.getAnnotation(RequestMapping.class).value();

                    System.out.println("Registered "+method.getName()+" request mapping");

                    if(requestMapping.containsKey(key)){
                        requestMapping.get(key).add(new ReflectMethod(listener, method));
                        requestMapping.get(key).sort(new PriorityComparator());
                        continue;
                    }

                    List<ReflectMethod> m = new ArrayList<>();
                    m.add(new ReflectMethod(listener, method));
                    requestMapping.put(key, m);
                }
            }
        }

    }

    public void registerRequestListener(Class<?> c)throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if(!RequestListener.class.isAssignableFrom(c)){//.getSuperclass().equals(RequestListener.class)){
            throw new IllegalArgumentException("Class '"+c.getSimpleName()+"' isn't a assignable from '"+RequestListener.class.getSimpleName()+"'");
        }

        registerRequestListener((RequestListener) c.getDeclaredConstructor().newInstance());
    }

    public void registerMessage(Class<?> c)throws NoSuchMethodException {
        if(!MessageBase.class.isAssignableFrom(c)){
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
    }

    public void join(int localPort, InetSocketAddress address)throws IOException {
        //bind(localPort);
        if(!server.isRunning()){
            server.start(localPort);
        }

        FindNodeRequest request = new FindNodeRequest();
        request.setDestination(address);
        request.setTarget(routingTable.getDerivedUID());
        server.send(request, new ResponseCallback(){
            @Override
            public void onResponse(ResponseEvent event){
                routingTable.insert(event.getNode());
                System.out.println("JOINED "+event.getNode());

                FindNodeResponse response = (FindNodeResponse) event.getMessage();

                if(response.hasNodes()){
                    List<Node> nodes = response.getAllNodes();

                    PingResponseListener listener = new PingResponseListener(routingTable);

                    long now = System.currentTimeMillis();
                    for(Node n : nodes){
                        if((getRoutingTable().isSecureOnly() && !n.hasSecureID()) || n.hasQueried(now)){
                            System.out.println("SKIPPING "+now+"  "+n.getLastSeen()+"  "+n);
                            continue;
                        }

                        PingRequest req = new PingRequest();
                        req.setDestination(n.getAddress());
                        try{
                            getServer().send(req, n, listener);
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }

                if(!refresh.isRunning()){
                    refresh.start();
                }
            }
        }); //WHAT ABOUT REFRESH... WE NEED A CALLBACK...
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
