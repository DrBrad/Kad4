package unet.kad4;

import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageBase;
import unet.kad4.messages.inter.MessageKey;
import unet.kad4.routing.BucketTypes;
import unet.kad4.routing.inter.RoutingTable;
import unet.kad4.rpc.RPCServer;
import unet.kad4.rpc.RefreshHandler;
import unet.kad4.rpc.events.inter.EventKey;
import unet.kad4.rpc.events.inter.MessageEvent;
import unet.kad4.rpc.events.inter.EventHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Kademlia {

    private RPCServer server;
    private RefreshHandler refresh;

    private Map<EventKey, List<Method>> eventListeners;
    private List<Class<?>> messages;
    //private DHT dht;

    //ALLOW DHT SPECIFICATION

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
        System.out.println("Starting with bucket type: "+routingTable.getClass().getSimpleName());
        server = new RPCServer(routingTable);
        refresh = new RefreshHandler();

        eventListeners = new HashMap<>();
        messages = new ArrayList<>();
        //refresh.addOperation(new BucketRefresh(server));
        //refresh.addOperation(new StaleRefresh(server));
        //new RPCHandler()
        //dht = new KDHT(server);
    }

    public void registerEventListener(Class<?> c){
        for(Method method : c.getDeclaredMethods()){
            if(method.isAnnotationPresent(EventHandler.class)){
                Parameter[] parameters = method.getParameters();

                if(parameters.length != 1){
                    continue;
                }

                if(parameters[0].getType().getSuperclass().equals(MessageEvent.class)){
                    EventKey key = new EventKey(method.getAnnotation(EventHandler.class));

                    System.out.println("Registered "+method.getName()+" event handler");

                    if(eventListeners.containsKey(key)){
                        eventListeners.get(key).add(method);
                        continue;
                    }

                    List<Method> m = new ArrayList<>();
                    m.add(method);
                    eventListeners.put(key, m);
                }
            }
        }
    }

    public void registerMessage(Class<?> c){
        if(!c.getSuperclass().equals(MessageBase.class)){
            throw new IllegalArgumentException("Class doesn't extend 'MessageBase' class");
        }

        if(!c.isAnnotationPresent(Message.class)){
            throw new IllegalArgumentException("Class is missing '@Message' annotation");
        }

        messages.add(c);
        System.out.println("Registered "+c.getSimpleName()+" message");
    }

    /*
    public void join(int localPort, InetAddress address, int port)throws SocketException {
        join(localPort, new InetSocketAddress(address, port));
    }

    public void join(int localPort, Node node)throws SocketException {
        join(localPort, node.getAddress());
    }

    public void join(int localPort, InetSocketAddress address)throws SocketException {
        //bind(localPort);
        if(!server.isRunning()){
            server.start(localPort);
        }
        //dht.join(address);

        new JoinOperation(server, refresh, address).run();
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
        *./
    }

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
