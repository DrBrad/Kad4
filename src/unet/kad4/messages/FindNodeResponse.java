package unet.kad4.messages;

import unet.bencode.variables.BencodeObject;
import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageException;
import unet.kad4.messages.inter.MessageType;
import unet.kad4.messages.inter.MethodMessageBase;
import unet.kad4.utils.Node;
import unet.kad4.utils.net.AddressType;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.ArrayList;
import java.util.List;

import static unet.kad4.utils.NodeUtils.packNodes;
import static unet.kad4.utils.NodeUtils.unpackNodes;

@Message(method = "find_node", type = MessageType.RSP_MSG)
public class FindNodeResponse extends MethodMessageBase {

    /*
    * THIS CLASS COULD BE MADE MUCH BETTER BUT IT WORKS FOR NOW...
    * */

    public static final int NODE_CAP = 20;
    private List<Node> nodes;

    public FindNodeResponse(byte[] tid){
        super(tid);//, Method.FIND_NODE, Type.RSP_MSG);
        nodes = new ArrayList<>();
        //ipv4Nodes = new ArrayList<>();
        //ipv6Nodes = new ArrayList<>();
    }

    @Override
    public BencodeObject encode(){
        BencodeObject ben = super.encode();
        //ben.getBencodeObject(message.type().innerKey()).put("target", target.getBytes());

        if(nodes.isEmpty()){
            return ben;
        }

        List<Node> nodes = getIPv4Nodes();
        if(!nodes.isEmpty()){
            ben.getBencodeObject(type.innerKey()).put("nodes", packNodes(nodes, AddressType.IPv4));
        }

        nodes = getIPv6Nodes();
        if(!nodes.isEmpty()){
            ben.getBencodeObject(type.innerKey()).put("nodes6", packNodes(nodes, AddressType.IPv6));
        }
        return ben;
    }

    @Override
    public void decode(BencodeObject ben)throws MessageException {
        super.decode(ben);

        if(!ben.getBencodeObject(type.innerKey()).containsKey("nodes") &&
                !ben.getBencodeObject(type.innerKey()).containsKey("nodes6")){
            throw new MessageException("Protocol Error, such as a malformed packet.", 203);
        }

        if(ben.getBencodeObject(type.innerKey()).containsKey("nodes")){
            nodes.addAll(unpackNodes(ben.getBencodeObject(type.innerKey()).getBytes("nodes"), AddressType.IPv4));
        }

        if(ben.getBencodeObject(type.innerKey()).containsKey("nodes6")){
            nodes.addAll(unpackNodes(ben.getBencodeObject(type.innerKey()).getBytes("nodes6"), AddressType.IPv6));
        }
    }

    public void addNode(Node node){
        nodes.add(node);
    }

    public Node getNode(int i){
        return nodes.get(i);
    }

    public void removeNode(Node node){
        nodes.remove(node);
    }

    public boolean containsNode(Node node){
        return nodes.contains(node);
    }

    public boolean hasNodes(){
        return !nodes.isEmpty();
    }

    public void addNodes(List<Node> nodes){
        if(nodes.size()+this.nodes.size() > NODE_CAP){
            throw new IllegalArgumentException("Adding nodes would exceed Node Cap of "+NODE_CAP);
        }

        this.nodes.addAll(nodes);
    }

    public List<Node> getAllNodes(){
        return nodes;
    }

    public List<Node> getIPv4Nodes(){
        List<Node> r = new ArrayList<>();

        for(Node node : nodes){
            if(node.getHostAddress() instanceof Inet4Address){
                r.add(node);
            }
        }
        return r;
    }

    public List<Node> getIPv6Nodes(){
        List<Node> r = new ArrayList<>();

        for(Node node : nodes){
            if(node.getHostAddress() instanceof Inet6Address){
                r.add(node);
            }
        }
        return r;
    }
}
