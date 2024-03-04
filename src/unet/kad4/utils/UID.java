package unet.kad4.utils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;

public class UID {

    public static final int ID_LENGTH = 20; //160
    protected byte[] bid;

    public UID(String key){
        int len = key.length();
        if(key.length() != ID_LENGTH*2){
            System.out.println();
            throw new IllegalArgumentException("Node ID is not correct length, given string is "+key.length()+" chars, required "+ID_LENGTH+" chars");
        }

        bid = new byte[ID_LENGTH];
        for(int i = 0; i < len; i += 2){
            bid[i/2] = (byte) ((Character.digit(key.charAt(i), 16) << 4)+Character.digit(key.charAt(i+1), 16));
        }
    }

    public UID(byte[] bid){
        if(bid.length != ID_LENGTH){
            throw new IllegalArgumentException("Key must be "+ID_LENGTH+" bytes");
        }

        this.bid = new byte[ID_LENGTH];
        System.arraycopy(bid, 0, this.bid, 0, ID_LENGTH);
    }


    public int getDistance(UID k){
        return ID_LENGTH-xor(k).getFirstSetBitIndex();
    }

    //COMPARE MATCHES
    public UID xor(UID k){
        byte[] distance = new byte[ID_LENGTH];

        for(int i = 0; i < ID_LENGTH; i++){
            distance[i] = (byte) (bid[i]^k.getBytes()[i]);
        }
        return new UID(distance);
    }

    public int getFirstSetBitIndex(){
        int prefixLength = 0;

        for(byte b : bid){
            if(b == 0){
                prefixLength += 8;
            }else{
                int count = 0;
                for(int i = 7; i >= 0; i--){
                    if((b & (1 << i)) == 0){
                        count++;
                    }else{
                        break;
                    }
                }

                prefixLength += count;
                break;
            }
        }
        return prefixLength;
    }

    public UID generateNodeIdByDistance(int distance){
        byte[] result = new byte[ID_LENGTH];

        int numByteZeroes = ((ID_LENGTH*8)-distance)/8;
        int numBitZeroes = 8-(distance%8);

        for(int i = 0; i < numByteZeroes; i++){
            result[i] = 0;
        }

        BitSet bits = new BitSet(8);
        bits.set(0, 8);

        for(int i = 0; i < numBitZeroes; i++){
            bits.clear(i);
        }
        bits.flip(0, 8);
        result[numByteZeroes] = bits.toByteArray()[0];

        for(int i = numByteZeroes+1; i < result.length; i++){
            result[i] = Byte.MAX_VALUE;
        }

        return xor(new UID(result));
    }

    public byte[] getBytes(){
        return bid;
    }

    /*
    public int getByte(int off){
        return bid[off];
    }
    */

    public BigInteger getInt(){
        return new BigInteger(1, bid);
    }

    /*
    public int getInt(int offset) {
        return Byte.toUnsignedInt(bid[offset]) << 24 |
                Byte.toUnsignedInt(bid[offset+1]) << 16 |
                Byte.toUnsignedInt(bid[offset+2]) << 8 |
                Byte.toUnsignedInt(bid[offset+3]);
    }
    */




    public String getBinary(){
        StringBuilder sb = new StringBuilder();
        for(byte b : bid){
            for(int i = 7; i >= 0; i--){
                sb.append(b >>> i & 1);
            }
        }

        return sb.toString();
    }

    public String getHex(){
        return String.format("%0"+(bid.length << 1)+"X", new BigInteger(1, bid));
    }



    @Override
    public int hashCode(){
        return (((bid[0] ^ bid[1] ^ bid[2] ^ bid[3] ^ bid[4]) & 0xff) << 24)
                | (((bid[5] ^ bid[6] ^ bid[7] ^ bid[8] ^ bid[9]) & 0xff) << 16)
                | (((bid[10] ^ bid[11] ^ bid[12] ^ bid[13] ^ bid[14]) & 0xff) << 8)
                | ((bid[15] ^ bid[16] ^ bid[17] ^ bid[18] ^ bid[19]) & 0xff);
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof UID){
            return Arrays.equals(bid, ((UID) o).bid);
        }
        return false;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder(ID_LENGTH*2);
        sb.append(String.format("%02x", bid[0])+String.format("%02x", bid[1])+String.format("%02x", bid[2])+" ");

        for(int i = 3; i < 19; i++){
            sb.append(String.format("%02x", bid[i]));
        }

        return sb.append(" "+String.format("%02x", bid[19])).toString();
    }
}
