package unet.kad4.utils.net;

import java.net.InetAddress;

public class NetMask {

    private byte[] address;
    private int mask;

    /*
    public static NetMask fromString(String toParse) {
        String[] parts = toParse.split("/");
        return new NetMask(unchecked(() -> InetAddress.getByName(parts[0])),Integer.valueOf(parts[1]));
    }
    */

    public NetMask(InetAddress address, int mask){
        this.address = address.getAddress();
        this.mask = mask;

        if(this.address.length*8 < mask){
            throw new IllegalArgumentException("Mask cannot cover more bits than the length of the network address.");
        }
    }

    public boolean contains(InetAddress oAddress){
        byte[] other = oAddress.getAddress();

        if(address.length != other.length){
            return false;
        }

        for(int i = 0; i < mask/8; i++){
            if(address[i] != other[i]){
                return false;
            }
        }

        if(mask%8 == 0){
            return true;
        }

        int offset = mask/8;
        int probeMask = (0xff00 >> mask%8) & 0xff;

        return (address[offset] & probeMask) == (other[offset] & probeMask);
    }
}
