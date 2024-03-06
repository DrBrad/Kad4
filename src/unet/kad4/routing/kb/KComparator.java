package unet.kad4.routing.kb;

import unet.kad4.utils.Node;
import unet.kad4.utils.UID;

import java.math.BigInteger;
import java.util.Comparator;

public class KComparator implements Comparator<Node> {

    public final BigInteger key;

    public KComparator(UID key){
        this.key = key.getInt();
    }

    @Override
    public int compare(Node a, Node b){
        BigInteger b1 = a.getUID().getInt();
        BigInteger b2 = b.getUID().getInt();

        b1 = b1.xor(key);
        b2 = b2.xor(key);

        return b1.abs().compareTo(b2.abs());
    }
}
