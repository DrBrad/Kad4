package unet.kad4.routing.kb;

import unet.kad4.utils.Node;

import java.util.Comparator;

public class LSComparetor implements Comparator<Node> {

    @Override
    public int compare(Node a, Node b){
        return (a.hashCode() == b.hashCode()) ? 0 : (a.getLastSeen() > b.getLastSeen()) ? 1 : -1;
        /*
        if(a.hashCode() == (b.hashCode())){
            return (a.hashCode() == b.hashCode()) ? 0 : (a.getLastSeen() > b.getLastSeen()) ? 1 : -1;
        }else{
            return (a.getLastSeen() > b.getLastSeen()) ? 1 : -1;
        }
        */
    }
}
