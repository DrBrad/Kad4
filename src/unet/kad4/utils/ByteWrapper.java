package unet.kad4.utils;

import java.util.Arrays;

public class ByteWrapper {

    private final byte[] b;
    private final int h;

    public ByteWrapper(byte[] b){
        this.b = b;
        h = Arrays.hashCode(b);
    }

    public byte[] getBytes(){
        return b;
    }

    @Override
    public int hashCode(){
        return h;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof ByteWrapper){
            return Arrays.equals(b, ((ByteWrapper)obj).b);
        }
        return false;
    }
}
