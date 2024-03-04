package unet.kad4.rpc;

import unet.kad4.operations.inter.Operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RefreshHandler {

    private Timer refreshTimer;
    private TimerTask refreshTimerTask;
    private List<Operation> operations;
    //private long refreshTime = 30000;
    private long refreshTime = 3600000;

    public RefreshHandler(){
        operations = new ArrayList<>();
    }

    public boolean isRunning(){
        return (refreshTimer != null || refreshTimerTask != null);
    }

    public void start(){
        if(isRunning()){
            throw new IllegalArgumentException("Refresh has already started.");
        }

        if(refreshTimer == null && refreshTimerTask == null){
            System.out.println("REFRESH STARTED");
            refreshTimer = new Timer(true);
            refreshTimerTask = new TimerTask(){
                @Override
                public void run(){
                    System.out.println("STARTING REFRESH");
                    for(Operation operation : operations){
                        operation.run();
                    }

                    /*
                    for(int i = 1; i < KID.ID_LENGTH; i++){
                        if(routingTable.getBucketSize(i) < KBucket.MAX_BUCKET_SIZE){ //IF THE BUCKET IS FULL WHY SEARCH... WE CAN REFILL BY OTHER PEER PINGS AND LOOKUPS...
                            final KID k = routingTable.getLocal().getKID().generateNodeIdByDistance(i);

                            final List<Node> closest = routingTable.findClosest(k, KBucket.MAX_BUCKET_SIZE);
                            if(!closest.isEmpty()){
                                exe.submit(new Runnable(){
                                    @Override
                                    public void run(){
                                        new NodeLookupMessage(routingTable, closest, k).execute();
                                    }
                                });
                            }
                        }
                    }

                    exe.submit(new Runnable(){
                        @Override
                        public void run(){
                            List<Contact> contacts = routingTable.getAllUnqueriedNodes();
                            if(!contacts.isEmpty()){
                                for(Contact c : contacts){
                                    new PingMessage(routingTable, c.getNode()).execute();
                                }
                            }
                        }
                    });

                    storage.evict();

                    final List<String> data = storage.getRenewal();
                    if(!data.isEmpty()){
                        for(final String r : data){
                            exe.submit(new Runnable(){
                                @Override
                                public void run(){
                                    try{
                                        new StoreMessage(KademliaNode.this, r).execute();
                                    }catch(NoSuchAlgorithmException e){
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                    */
                }
            };

            refreshTimer.schedule(refreshTimerTask, 0, refreshTime); //MAKE DELAY LONG, HOWEVER PERIOD AROUND 1 HOUR
        }
    }

    public void stop(){
        if(refreshTimerTask != null){
            refreshTimerTask.cancel();
        }

        if(refreshTimer != null){
            refreshTimer.cancel();
            refreshTimer.purge();
        }
    }

    public long getRefreshTime(){
        return refreshTime;
    }

    public void setRefreshTime(long time){
        refreshTime = time;
    }

    public void addOperation(Operation operation){
        operations.add(operation);
    }

    public boolean removeOperation(Operation operation){
        return operations.remove(operation);
    }

    public Operation getOperation(int i){
        return operations.get(i);
    }

    public boolean containsOperation(Operation operation){
        return operations.contains(operation);
    }
}
