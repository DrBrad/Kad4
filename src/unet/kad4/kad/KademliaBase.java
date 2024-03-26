package unet.kad4.kad;

import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageBase;
import unet.kad4.messages.inter.MessageKey;
import unet.kad4.refresh.RefreshHandler;
import unet.kad4.routing.BucketTypes;
import unet.kad4.routing.inter.RoutingTable;
import unet.kad4.rpc.RequestListener;
import unet.kad4.rpc.events.inter.MessageEvent;
import unet.kad4.rpc.events.inter.PriorityComparator;
import unet.kad4.rpc.events.inter.RequestMapping;
import unet.kad4.utils.Node;
import unet.kad4.utils.ReflectMethod;

import java.io.IOException;
import java.lang.reflect.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KademliaBase {


    protected RoutingTable routingTable;
    protected Server server;
    protected RefreshHandler refresh;
    protected Map<String, List<ReflectMethod>> requestMapping;
    protected Map<MessageKey, Constructor<?>> messages;

    public KademliaBase(){
        this(BucketTypes.KADEMLIA.getRoutingTable());
    }

    public KademliaBase(String bucketType){
        this(BucketTypes.fromString(bucketType).getRoutingTable());
    }

    public KademliaBase(RoutingTable routingTable){
        this.routingTable = routingTable;
        System.out.println("Starting with bucket type: "+routingTable.getClass().getSimpleName());
        server = new Server(this);
        refresh = new RefreshHandler(this);

        requestMapping = new HashMap<>();
        messages = new HashMap<>();
    }

    public void registerRequestListener(RequestListener listener)throws NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        Field f = RequestListener.class.getDeclaredField("kademlia");
        f.setAccessible(true);
        f.set(listener, this);

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

    public void registerRequestListener(Class<?> c)throws NoSuchFieldException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
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

    public void join(int localPort, InetAddress address, int port)throws IOException {
        join(localPort, new InetSocketAddress(address, port));
    }

    public void join(int localPort, Node node)throws IOException {
        join(localPort, node.getAddress());
    }

    public void join(int localPort, InetSocketAddress address)throws IOException {
        if(!server.isRunning()){
            server.start(localPort);
        }
    }

    public void bind()throws SocketException {
        bind(0);
    }

    public void bind(int port)throws SocketException {
        if(!server.isRunning()){
            server.start(port);
        }
    }

    public void stop(){
        server.stop();
        refresh.stop();
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
}
