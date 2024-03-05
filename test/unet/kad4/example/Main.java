package unet.kad4.example;

import unet.kad4.Kademlia;
import unet.kad4.rpc.EventListener;
import unet.kad4.rpc.KEventListener;
import unet.kad4.rpc.events.inter.EventHandler;
import unet.kad4.rpc.events.inter.EventKey;
import unet.kad4.rpc.events.inter.MessageEvent;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Main {

    /*
     * LOCAL VS. EXTERNAL
     *
     * WE WILL ALWAYS ACCEPT ANYTHING THAT HAS A VERIFIED ID AND IS PINGABLE
     *
     * - NOTE
     *     THIS MEANS THAT I CAN ADD A LOCAL NODE BUT ONLY ON A LOCAL DHT - IF THE MAJORITY OF THE NODES ARE EXTERNAL I
     *     CANNOT ADD THE NODE AS THE ID WILL BE INVALID FOR THE LOCAL NODE
     *
     *     EXAMPLE
     *       I192.168.0.7 E96.97.98.99 -> PINGs 192.168.0.8
     *       I192.168.0.8 E96.97.98.99 WILL NOT ADD TO BUCKET
     *
     *     FACTS
     *       WE WILL NEVER RECEIVE A LOCAL IP OF A NODE ON OUR NETWORK FROM AN EXTERNAL NODE
     *
     *       EXAMPLE
     *         E96.97.98.99 Will never receive a packet from E96.97.96.97 about one of our local I192.168.0.???
     *
     *     IT MIGHT BE SMART THAT IF WE FIND_NODEs ON AN EXTERNAL NODE NOT TO EVEN TRY PINGING OR VERIFYING LOCAL_NODES
     *
     */

    //REMEMBER THREAD POOL...

    //WOULD IT BE BETTER TO NOT USE POOLS BUT JUST TAKE EACH PACKET AND SEND IT TO A NEW THREAD...?

    //START COUNTING MESSAGES RECEIVED SO THAT WE CAN DETERMINE IF WE ARE FAILING WITH ROUTING TABLE CONSENSUS IP
    //THE IP TAKES LIKE 10 REFRESHES TO GET IP - LIKELY THE ASYNC RESPONSE IS THROWING THIS UPDATE OFF

    //TRY NOT TO QUERY A NODE MORE THAN 2 TIMES WITH SAME REQUEST
    //WORK ON SPAM THROTTLE AND BLACK HOLE

    /*
    Spam throttle
    Blackhole
    Teredo handler

    Dht
    UPnP

    Dont auto set LAST SEEN TO - TIME STAMP USE BETTER METHOD...
    Make the decode better

    Can we handle the stalling better...?

    Bring bind back
    FindNodeResponse fix
    */

    public static void main(String[] args){
        try{

            Kademlia k = new Kademlia("Kademlia");
            //k.registerEventListener(ReceiverListener.class);
            //k.registerMessage(GetPeersRequest.class);
            k.join(6881, InetAddress.getByName("router.bittorrent.com"), 6881);




            //k.setDHT(KDHT.class);
            //k.bind(8080);

            /*
            k.join(6881, InetAddress.getByName("router.bittorrent.com"), 6881);

            while(true){
                Thread.sleep(10000);
                System.out.println("CONSENSUS: "+k.getUID()+"  "+k.getConsensusAddress().getHostAddress()+"  "+k.getRouterSize());
            }*/

            //UID uid = k.getDHT().getUID();
            //System.out.println(uid);

            //Node n = new Node(uid, Inet4Address.getLocalHost(), 8080);
            //System.out.println("Has secure ID: "+n.hasSecureID());

            //k.stop();

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
