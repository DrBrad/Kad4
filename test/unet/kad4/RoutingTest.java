package unet.kad4;

import unet.kad4.routing.inter.RoutingTable;
import unet.kad4.routing.kb.KBucket;
import unet.kad4.routing.kb.KRoutingTable;
import unet.kad4.utils.Node;
import unet.kad4.utils.UID;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class RoutingTest {

    public static void main(String[] args)throws UnknownHostException {
        RoutingTable r = new KRoutingTable();
        r.deriveUID();

        insert(r);
        consensus(r);
    }

    public static void insert(RoutingTable r)throws UnknownHostException {
        r.insert(new Node("5fbfbff10c5d6a4ec8a88e4c6ab4c28b95eee401", InetAddress.getByName("124.31.75.21"), 1));
        r.insert(new Node("5a3ce9c14e7a08645677bbd1cfe7d8f956d53256", InetAddress.getByName("21.75.31.124"), 1));
        r.insert(new Node("a5d43220bc8f112a3d426c84764f8c2a1150e616", InetAddress.getByName("65.23.51.170"), 1));
        r.insert(new Node("1b0321dd1bb1fe518101ceef99462b947a01ff41", InetAddress.getByName("84.124.73.14"), 1));
        r.insert(new Node("e56f6cbf5b7c4be0237986d5243b87aa6d51305a", InetAddress.getByName("43.213.53.83"), 1));

        r.insert(new Node("a6d7bb7e57492f7a3d8e0a7d8de179058b9b9605", InetAddress.getByName("186.114.205.58"), 1));
        r.insert(new Node("9ec40e2f13003bdfea3b095d4a81854228f04999", InetAddress.getByName("55.121.36.140"), 1));
        r.insert(new Node("345db50bf8b125f9b33ccea916a35f79ea4fe9c8", InetAddress.getByName("200.0.237.192"), 1));
        r.insert(new Node("0219fe707145540bfe502a55ca98bde78d48e2ac", InetAddress.getByName("50.184.60.76"), 1));
        r.insert(new Node("b8afd87bb301a67fc3a25dc823ba486d301f523c", InetAddress.getByName("110.9.17.14"), 1));

        r.insert(new Node("5d4ba49d512b78e64df066699b57f4612af34783", InetAddress.getByName("225.150.72.107"), 1));
        r.insert(new Node("38ddd9a2359f6d499437fedebc85157245758df8", InetAddress.getByName("236.72.56.71"), 1));
        r.insert(new Node("44a35ae4ae8540c0f0327ab86091b50c336d3478", InetAddress.getByName("107.222.209.217"), 1));
        r.insert(new Node("907f6dac4afa7f0e906390dab4bd94585f61d186", InetAddress.getByName("115.61.238.72"), 1));
        r.insert(new Node("84bc54cdbe10f4040a3ea4f6ade5fafe02c3d8cc", InetAddress.getByName("211.55.90.189"), 1));

        r.insert(new Node("325ae0d18931c61f93ebab19e289e00a30abe6ec", InetAddress.getByName("177.222.15.120"), 1));
        r.insert(new Node("2e5d51b6a3d5156a38ad27929a4e6639e1313377", InetAddress.getByName("70.217.219.175"), 1));
        r.insert(new Node("636ee4a141ac9b8f5c543a1baabdeec7545593e1", InetAddress.getByName("3.216.83.255"), 1));
        r.insert(new Node("54848c7a2e409f611c0109bc760d6e7b6ee65493", InetAddress.getByName("245.158.65.102"), 1));
        r.insert(new Node("83f072d84090dcb83981a7d88c9c7a6920cf3cfe", InetAddress.getByName("146.14.113.231"), 1));

        r.insert(new Node("11b9d53a6b2ed991b0846a9847eb4541e6d75c93", InetAddress.getByName("112.106.57.62"), 1));
        r.insert(new Node("a110b74c797300ddd630ef2e5e594ee482df14e1", InetAddress.getByName("151.58.161.102"), 1));
        r.insert(new Node("f463627d9d68b11d2ed8421ec9cae2eddac2001f", InetAddress.getByName("250.221.0.142"), 1));
        r.insert(new Node("5a96d1d6bb0d138bab3747e6192a1802ec6f4676", InetAddress.getByName("89.52.19.81"), 1));
        r.insert(new Node("e0bb8b71eff395ef327fe92d235c16a894bb1902", InetAddress.getByName("11.35.168.232"), 1));

        r.insert(new Node("e6ca6283cde4edf3e8ec991c01ce5714123dbad3", InetAddress.getByName("29.53.128.244"), 1));
        r.insert(new Node("3ea05c4505c6b90c0bea7a45bc46fa3d4825cb6d", InetAddress.getByName("19.163.1.25"), 1));
        r.insert(new Node("700caeae41e056292b6e4f042deebf0e71dac85d", InetAddress.getByName("73.144.25.50"), 1));
        r.insert(new Node("112fefdf141dd48e3b256e10dc710bc0507f13a1", InetAddress.getByName("227.102.96.83"), 1));
        r.insert(new Node("7b5efed771dd6966e280bb24c531395a5ba1dea8", InetAddress.getByName("188.112.7.200"), 1));

        r.insert(new Node("a82377f6b3b3ddd56555b4b0b71c026ecae02c05", InetAddress.getByName("166.45.173.183"), 1));
        r.insert(new Node("554ba99e176d559028992543d19b196c53f27944", InetAddress.getByName("86.88.54.2"), 1));
        r.insert(new Node("3fb30bdc6ac02e1bda445597cefbe46218c57a40", InetAddress.getByName("103.170.64.115"), 1));
        r.insert(new Node("c8879c8f193584e921ea34d3324decf647027752", InetAddress.getByName("33.13.77.5"), 1));
        r.insert(new Node("3c64faf367487b474b5169f07cf197ddf335b860", InetAddress.getByName("186.60.165.238"), 1));

        //CHECKING KNOWN BAD IDS
        //THE INSECURE SHOULDN'T BE PASSED INTO THE ROUTING TABLE - TOTAL NODES SHOULD BE 5
        /*
        r.insert(new Node("5cbfbff10c5d6a4ec8a88e4c6ab4c28b95eee401", InetAddress.getByName("124.31.75.21"), 1));
        r.insert(new Node("5c3ce9c14e7a08645677bbd1cfe7d8f956d53256", InetAddress.getByName("21.75.31.124"), 1));
        r.insert(new Node("acd43220bc8f112a3d426c84764f8c2a1150e616", InetAddress.getByName("65.23.51.170"), 1));
        r.insert(new Node("1c0321dd1bb1fe518101ceef99462b947a01ff41", InetAddress.getByName("84.124.73.14"), 1));
        r.insert(new Node("ec6f6cbf5b7c4be0237986d5243b87aa6d51305a", InetAddress.getByName("43.213.53.83"), 1));
        */

        List<Node> nodes = r.findClosest(new UID("afd43220bc8f112a3d426c84764f8c2a1150e616"), KBucket.MAX_BUCKET_SIZE);

        for(Node n : nodes){
            System.out.println(n);
        }
    }

    public static void consensus(RoutingTable r)throws UnknownHostException {
        InetAddress external = InetAddress.getByName("186.60.165.238");
        r.updatePublicIPConsensus(InetAddress.getByName("124.31.75.21"), external);
        r.updatePublicIPConsensus(InetAddress.getByName("21.75.31.124"), external);
        r.updatePublicIPConsensus(InetAddress.getByName("21.75.31.124"), external);
        r.updatePublicIPConsensus(InetAddress.getByName("84.124.73.14"), external);
        r.updatePublicIPConsensus(InetAddress.getByName("43.213.53.83"), external);

        r.updatePublicIPConsensus(InetAddress.getByName("186.114.205.58"), InetAddress.getByName("33.13.77.5"));
        r.updatePublicIPConsensus(InetAddress.getByName("55.121.36.140"), InetAddress.getByName("33.13.77.5"));
        r.updatePublicIPConsensus(InetAddress.getByName("200.0.237.192"), InetAddress.getByName("33.13.77.5"));
        r.updatePublicIPConsensus(InetAddress.getByName("50.184.60.76"), InetAddress.getByName("33.13.77.5"));
        r.updatePublicIPConsensus(InetAddress.getByName("110.9.17.14"), InetAddress.getByName("33.13.77.5"));

        r.updatePublicIPConsensus(InetAddress.getByName("225.150.72.107"), external);
        r.updatePublicIPConsensus(InetAddress.getByName("236.72.56.71"), external);
        r.updatePublicIPConsensus(InetAddress.getByName("107.222.209.217"), external);
        r.updatePublicIPConsensus(InetAddress.getByName("115.61.238.72"), external);
        r.updatePublicIPConsensus(InetAddress.getByName("211.55.90.189"), external);

        r.updatePublicIPConsensus(InetAddress.getByName("177.222.15.120"), external);
        r.updatePublicIPConsensus(InetAddress.getByName("70.217.219.175"), external);
        r.updatePublicIPConsensus(InetAddress.getByName("3.216.83.255"), external);
        r.updatePublicIPConsensus(InetAddress.getByName("245.158.65.102"), external);
        r.updatePublicIPConsensus(InetAddress.getByName("146.14.113.231"), external);

        r.updatePublicIPConsensus(InetAddress.getByName("112.106.57.62"), external);
        r.updatePublicIPConsensus(InetAddress.getByName("151.58.161.102"), external);
        r.updatePublicIPConsensus(InetAddress.getByName("250.221.0.142"), external);
        r.updatePublicIPConsensus(InetAddress.getByName("89.52.19.81"), external);
        r.updatePublicIPConsensus(InetAddress.getByName("11.35.168.232"), external);

        System.out.println(r.getConsensusExternalAddress());
    }
}
