package unet.kad4.example;

import unet.kad4.rpc.RequestListener;
import unet.kad4.rpc.events.RequestEvent;
import unet.kad4.rpc.events.inter.RequestMapping;

public class ReceiverListener extends RequestListener {

    @RequestMapping("ping")
    public void onReceive(RequestEvent event){
        //event.preventDefault();
        System.out.println("PING "+event.getNode());
    }


    @RequestMapping("find_node")
    public void onReceive2(RequestEvent event){
        //event.preventDefault();
        System.out.println("FIND_NODE "+event.getNode());
    }
}
