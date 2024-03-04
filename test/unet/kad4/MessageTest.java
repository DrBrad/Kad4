package unet.kad4;

import unet.kad4.messages.*;
import unet.kad4.messages.MessageDecoder;
import unet.kad4.messages.inter.MessageBase;
import unet.kad4.messages.inter.MessageException;
import unet.kad4.utils.Node;
import unet.kad4.utils.UID;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static unet.kad4.rpc.RPCServer.TID_LENGTH;

public class MessageTest {

    public static void main(String[] args)throws UnknownHostException, MessageException {
        UID uid = new UID("992c105ffed716245654fd4c2c4d71e0f2df58cc");
        //pingRequest(uid);
        //findNodeRequest(uid);
        //checkError();
    }

    /*
    public static void pingRequest(UID uid)throws UnknownHostException, MessageException {
        PingRequest r = new PingRequest(new byte[TID_LENGTH]);
        r.setUID(uid);
        System.out.println("PING REQUEST:");
        System.out.println(r);
        checkRequest(r);

        pingResponse(r, uid);
    }

    public static void findNodeRequest(UID uid)throws UnknownHostException, MessageException {
        FindNodeRequest r = new FindNodeRequest(new byte[TID_LENGTH]);
        r.setUID(uid);
        r.setTarget(new UID("5a3ce9c14e7a08645677bbd1cfe7d8f956d53256"));
        System.out.println("FIND_NODE REQUEST:");
        System.out.println(r);
        checkRequest(r);

        findNodeResponse(r, uid);
    }

    public static void pingResponse(PingRequest m, UID uid)throws UnknownHostException, MessageException {
        PingResponse r = new PingResponse(m.getTransactionID());
        r.setUID(uid);
        r.setPublic(Inet4Address.getLocalHost(), 8080);
        System.out.println("PING RESPONSE:");
        System.out.println(r);
        //checkResponse(m.getMethod(), r);
    }

    public static void findNodeResponse(FindNodeRequest m, UID uid)throws UnknownHostException, MessageException {
        FindNodeResponse r = new FindNodeResponse(m.getTransactionID());
        r.setUID(uid);
        r.setPublic(Inet4Address.getLocalHost(), 8080);

        //CHECKING KNOWN GOOD IDS
        r.addNode(new Node("5fbfbff10c5d6a4ec8a88e4c6ab4c28b95eee401", InetAddress.getByName("124.31.75.21"), 1));
        r.addNode(new Node("5a3ce9c14e7a08645677bbd1cfe7d8f956d53256", InetAddress.getByName("21.75.31.124"), 1));
        r.addNode(new Node("a5d43220bc8f112a3d426c84764f8c2a1150e616", InetAddress.getByName("65.23.51.170"), 1));
        r.addNode(new Node("1b0321dd1bb1fe518101ceef99462b947a01ff41", InetAddress.getByName("84.124.73.14"), 1));
        r.addNode(new Node("e56f6cbf5b7c4be0237986d5243b87aa6d51305a", InetAddress.getByName("43.213.53.83"), 1));

        //CHECKING KNOWN BAD IDS
        r.addNode(new Node("5cbfbff10c5d6a4ec8a88e4c6ab4c28b95eee401", InetAddress.getByName("124.31.75.21"), 1));
        r.addNode(new Node("5c3ce9c14e7a08645677bbd1cfe7d8f956d53256", InetAddress.getByName("21.75.31.124"), 1));
        r.addNode(new Node("acd43220bc8f112a3d426c84764f8c2a1150e616", InetAddress.getByName("65.23.51.170"), 1));
        r.addNode(new Node("1c0321dd1bb1fe518101ceef99462b947a01ff41", InetAddress.getByName("84.124.73.14"), 1));
        r.addNode(new Node("ec6f6cbf5b7c4be0237986d5243b87aa6d51305a", InetAddress.getByName("43.213.53.83"), 1));

        System.out.println("FIND_NODE RESPONSE:");
        System.out.println(r);
        //checkResponse(m.getMethod(), r);
    }

    public static void checkError()throws MessageException {
        ErrorMessage r = new ErrorMessage(new byte[TID_LENGTH]);
        r.setErrorType(ErrorMessage.ErrorType.PROTOCOL);
        System.out.println("FIND_NODE REQUEST:");
        System.out.println(r);
        checkError(r);
    }

    public static void checkRequest(MessageBase m)throws MessageException {
        byte[] b = m.encode();
        m = new MessageDecoder(b).decodeRequest();
        System.out.println("Encoding > Decode Match: "+matches(b, m.encode()));
        System.out.println();
        System.out.println();
    }

    public static void checkResponse(/*MessageBase.Method t, *./MessageBase m)throws MessageException {
        byte[] b = m.encode();
        //m = new MessageDecoder(b).decodeResponse(t);
        System.out.println("Encoding > Decode Match: "+matches(b, m.encode()));
        System.out.println();
        System.out.println();
    }

    public static void checkError(MessageBase m)throws MessageException {
        byte[] b = m.encode();
        m = new MessageDecoder(b).decodeError();
        System.out.println("Encoding > Decode Match: "+matches(b, m.encode()));
        System.out.println();
        System.out.println();
    }

    public static boolean matches(byte[] a, byte[] b){
        return Arrays.equals(digestBytes("SHA1", a), digestBytes("SHA1", b));
    }

    private static byte[] digestBytes(String algorithm, byte[] input){
        try{
            MessageDigest m = MessageDigest.getInstance(algorithm);
            m.update(input, 0, input.length);
            return m.digest();

        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return null;
    }*/
}
