package unet.kad4.messages;

import unet.kad4.libs.bencode.variables.BencodeObject;
import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageBase;
import unet.kad4.messages.inter.MessageType;
import unet.kad4.utils.Node;

import java.util.List;

@Message(method = "find_node", type = MessageType.RSP_MSG)
public class FindNodeResponse extends MessageBase {

    /*
    * THIS CLASS COULD BE MADE MUCH BETTER BUT IT WORKS FOR NOW...
    * */

    public static final int NODE_CAP = 20;
    private List<Node> nodes;

    public FindNodeResponse(byte[] tid){
        super(tid);//, Method.FIND_NODE, Type.RSP_MSG);
        //ipv4Nodes = new ArrayList<>();
        //ipv6Nodes = new ArrayList<>();
    }

    @Override
    public BencodeObject encode(){
        BencodeObject ben = super.encode();
        //ben.getBencodeObject(message.type().innerKey()).put("target", target.getBytes());
        return ben;
    }

    @Override
    public void decode(BencodeObject ben){
        super.decode(ben);

        if(!ben.getBencodeObject(message.type().innerKey()).containsKey("nodes") &&
                !ben.getBencodeObject(message.type().innerKey()).containsKey("nodes6")){
            //throw new MessageException("Response to "+FIND_NODE+" did not contain 'node' or 'node6'", ErrorMessage.ErrorType.PROTOCOL);
        }

        if(ben.getBencodeObject(message.type().innerKey()).containsKey("nodes")){
            //((FindNodeResponse) message).addNodes(unpackNodes(ben.getBencodeObject(message.type().innerKey()).getBytes("nodes"), AddressType.IPv4), AddressType.IPv4);
        }

        if(ben.getBencodeObject(message.type().innerKey()).containsKey("nodes6")){
            //((FindNodeResponse) message).addNodes(unpackNodes(ben.getBencodeObject(message.type().innerKey()).getBytes("nodes6"), AddressType.IPv6), AddressType.IPv6);
        }
        /*
        if(!ben.getBencodeObject(message.type().innerKey()).containsKey("target")){
            System.out.println("MISSING TARGET");
        }

        target = new UID(ben.getBencodeObject(message.type().innerKey()).getBytes("target"));
        */
    }

    /*
    public void addNode(Node node){
        if(node.getHostAddress() instanceof Inet4Address){
            if(ipv4Nodes.size() > NODE_CAP){
                throw new IllegalArgumentException("Node cap already reached, the node cap is "+NODE_CAP);
            }

            ipv4Nodes.add(node);
            return;
        }

        if(ipv6Nodes.size() > NODE_CAP){
            throw new IllegalArgumentException("Node cap already reached, the node cap is "+NODE_CAP);
        }

        ipv6Nodes.add(node);
    }

    public void addNodes(List<Node> nodes){
        for(Node n : nodes){
            if(n.getHostAddress() instanceof Inet4Address){
                if(ipv4Nodes.size() < NODE_CAP){
                    ipv4Nodes.add(n);
                }
                continue;
            }

            if(ipv6Nodes.size() < NODE_CAP){
                ipv6Nodes.add(n);
            }
        }
    }

    public void addNodes(List<Node> nodes, AddressType type){
        switch(type){
            case IPv4:
                ipv4Nodes.addAll(nodes);
                break;

            case IPv6:
                ipv6Nodes.addAll(nodes);
                break;
        }
    }

    public boolean containsNode(Node node){
        if(node.getHostAddress() instanceof Inet4Address){
            return ipv4Nodes.contains(node);
        }
        return ipv6Nodes.contains(node);
    }

    public boolean removeNode(Node node){
        if(node.getHostAddress() instanceof Inet4Address){
            return ipv4Nodes.remove(node);
        }
        return ipv6Nodes.remove(node);
    }

    public List<Node> getIPv4Nodes(){
        return ipv4Nodes;
    }

    public List<Node> getIPv6Nodes(){
        return ipv6Nodes;
    }

    public List<Node> getAllNodes(){
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(ipv4Nodes);
        nodes.addAll(ipv6Nodes);
        return nodes;
    }
    */

    /*
    @Override
    public BencodeObject getBencode(){
        BencodeObject ben = super.getBencode();

        if(!ipv4Nodes.isEmpty()){
            ben.getBencodeObject(t.innerKey()).put("nodes", packNodes(ipv4Nodes, AddressType.IPv4));
        }

        if(!ipv6Nodes.isEmpty()){
            ben.getBencodeObject(t.innerKey()).put("nodes6", packNodes(ipv6Nodes, AddressType.IPv6));
        }

        return ben;
    }
    */
}
