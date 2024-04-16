package unet.kad4.routing.mainline;

import unet.kad4.utils.Node;

import java.util.ArrayList;
import java.util.List;

public class MBucket {

    //TODO

    private List<Node> nodes;
    public static final int MAX_BUCKET_SIZE = 5;

    public MBucket(){
        nodes = new ArrayList<>(MAX_BUCKET_SIZE);
    }

    public void insert(Node n){
        nodes.add(n);
    }

    public Node get(int i){
        return nodes.get(i);
    }

    public void remove(Node n){
        if(nodes.contains(n)){
            nodes.remove(n);
        }
    }

    public int size(){
        return nodes.size();
    }

    public boolean isFull(){
        return nodes.size() > 4;
    }

    public List<Node> list(){
        return nodes;
    }
}
