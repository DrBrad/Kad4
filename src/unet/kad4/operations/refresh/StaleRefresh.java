package unet.kad4.operations.refresh;

import unet.kad4.operations.PingOperation;
import unet.kad4.operations.inter.Operation;
import unet.kad4.rpc.RPCServer;
import unet.kad4.utils.Node;

import java.util.List;

public class StaleRefresh {/*implements Operation {

    private RPCServer server;

    public StaleRefresh(RPCServer server){
        this.server = server;
    }

    @Override
    public void run(){
        List<Node> nodes = server.getRoutingTable().getAllUnqueriedNodes();
        if(nodes.isEmpty()){
            return;
        }
        new PingOperation(server, nodes).run();
    }*/
}
