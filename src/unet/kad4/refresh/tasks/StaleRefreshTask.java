package unet.kad4.refresh.tasks;

import unet.kad4.Kademlia;
import unet.kad4.refresh.tasks.inter.Task;

public class StaleRefreshTask extends Task {

    public StaleRefreshTask(Kademlia kademlia){
        super(kademlia);
    }

    @Override
    public void execute(){

    }
}
