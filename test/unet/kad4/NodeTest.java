package unet.kad4;

import unet.kad4.utils.Node;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodeTest {

    public static void main(String[] args)throws UnknownHostException {
        //CHECKING KNOWN GOOD IDS
        System.out.println("SECURE");
        System.out.println(new Node("5fbfbff10c5d6a4ec8a88e4c6ab4c28b95eee401", InetAddress.getByName("124.31.75.21"), 1));
        System.out.println(new Node("5a3ce9c14e7a08645677bbd1cfe7d8f956d53256", InetAddress.getByName("21.75.31.124"), 1));
        System.out.println(new Node("a5d43220bc8f112a3d426c84764f8c2a1150e616", InetAddress.getByName("65.23.51.170"), 1));
        System.out.println(new Node("1b0321dd1bb1fe518101ceef99462b947a01ff41", InetAddress.getByName("84.124.73.14"), 1));
        System.out.println(new Node("e56f6cbf5b7c4be0237986d5243b87aa6d51305a", InetAddress.getByName("43.213.53.83"), 1));
        System.out.println();

        //CHECKING KNOWN BAD IDS
        System.out.println("INSECURE");
        System.out.println(new Node("5cbfbff10c5d6a4ec8a88e4c6ab4c28b95eee401", InetAddress.getByName("124.31.75.21"), 1));
        System.out.println(new Node("5c3ce9c14e7a08645677bbd1cfe7d8f956d53256", InetAddress.getByName("21.75.31.124"), 1));
        System.out.println(new Node("acd43220bc8f112a3d426c84764f8c2a1150e616", InetAddress.getByName("65.23.51.170"), 1));
        System.out.println(new Node("1c0321dd1bb1fe518101ceef99462b947a01ff41", InetAddress.getByName("84.124.73.14"), 1));
        System.out.println(new Node("ec6f6cbf5b7c4be0237986d5243b87aa6d51305a", InetAddress.getByName("43.213.53.83"), 1));
    }
}
