package unet.kad4.libs.bencode;

import unet.kad4.libs.bencode.variables.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bencoder {

    private byte[] buf;
    private int pos = 0;

    public byte[] encode(BencodeArray l){
        buf = new byte[l.byteSize()];
        put(l);
        return buf;
    }

    public byte[] encode(BencodeObject m){
        buf = new byte[m.byteSize()];
        put(m);
        return buf;
    }

    public List<BencodeVariable> decodeArray(byte[] buf, int off){
        this.buf = buf;
        pos = off;
        return decodeArray();
    }

    public Map<BencodeBytes, BencodeVariable> decodeObject(byte[] buf, int off){
        this.buf = buf;
        pos = off;
        return decodeObject();
    }

    private void put(BencodeVariable v){
        if(v instanceof BencodeBytes){
            put((BencodeBytes) v);
        }else if(v instanceof BencodeNumber){
            put((BencodeNumber) v);
        }else if(v instanceof BencodeArray){
            put((BencodeArray) v);
        }else if(v instanceof BencodeObject){
            put((BencodeObject) v);
        }
    }

    private void put(BencodeBytes v){
        byte[] b = v.getBytes();
        System.arraycopy(b, 0, buf, pos, b.length);
        pos += b.length;
    }

    private void put(BencodeNumber n){
        byte[] b = n.getBytes();
        System.arraycopy(b, 0, buf, pos, b.length);
        pos += b.length;
    }

    private void put(BencodeArray l){
        buf[pos] = 'l';
        pos++;

        for(int i = 0; i < l.size(); i++){
            put(l.valueOf(i));
        }
        buf[pos] = 'e';
        pos++;
    }

    private void put(BencodeObject m){
        buf[pos] = 'd';
        pos++;

        for(BencodeBytes k : m.keySet()){
            put(k);
            put(m.valueOf(k));
        }
        buf[pos] = 'e';
        pos++;
    }

    private List<BencodeVariable> decodeArray(){
        if(buf[pos] == 'l'){
            ArrayList<BencodeVariable> a = new ArrayList<>();
            pos++;

            while(buf[pos] != 'e'){
                a.add(get());
            }
            pos++;
            return a;
        }
        return null;
    }

    private Map<BencodeBytes, BencodeVariable> decodeObject(){
        if(buf[pos] == 'd'){
            HashMap<BencodeBytes, BencodeVariable> m = new HashMap<>();
            pos++;

            while(buf[pos] != 'e'){
                m.put(getBytes(), get());
            }
            pos++;
            return m;
        }
        return null;
    }

    private BencodeVariable get(){
        switch(buf[pos]){
            case 'i':
                return getNumber();
            case 'l':
                return getList();
            case 'd':
                return getMap();
            default:
                if(buf[pos] >= '0' && buf[pos] <= '9'){
                    return getBytes();
                }
        }
        return null;
    }

    private BencodeNumber getNumber(){
        char[] c = new char[32];
        pos++;
        int s = pos;
        while(buf[pos] != 'e'){
            c[pos-s] = (char) buf[pos];
            pos++;
        }

        pos++;
        return new BencodeNumber(new String(c, 0, pos-s-1));
    }

    private BencodeBytes getBytes(){
        char[] c = new char[8];
        int s = pos;
        while(buf[pos] != ':'){
            c[pos-s] = (char) buf[pos];
            pos++;
        }

        int t = Integer.parseInt(new String(c, 0, pos-s));
        byte[] b = new byte[t];
        System.arraycopy(buf, pos+1, b, 0, b.length);
        pos += t+1;

        return new BencodeBytes(b);
    }

    private BencodeArray getList(){
        return new BencodeArray(decodeArray());
    }

    private BencodeObject getMap(){
        return new BencodeObject(decodeObject());
    }
}
