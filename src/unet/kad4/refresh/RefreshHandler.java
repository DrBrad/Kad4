package unet.kad4.refresh;

import unet.kad4.Kademlia;
import unet.kad4.refresh.tasks.inter.Task;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RefreshHandler {

    private Kademlia kademlia;
    private Timer refreshTimer;
    private TimerTask refreshTimerTask;
    private List<Task> tasks;
    //private long refreshTime = 30000;
    private long refreshTime = 3600000;

    public RefreshHandler(Kademlia kademlia){
        this.kademlia = kademlia;
        tasks = new ArrayList<>();
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
                    for(Task task : tasks){
                        task.execute();
                    }
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

    public void addOperation(Task task)throws NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        Field f = Task.class.getDeclaredField("kademlia");
        f.setAccessible(true);
        f.set(task, kademlia);

        tasks.add(task);
    }

    public void addOperation(Class<?> c)throws NoSuchFieldException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if(!Task.class.isAssignableFrom(c)){
            throw new IllegalArgumentException("Class '"+c.getSimpleName()+"' isn't a super of 'Task'");
        }

        addOperation((Task) c.getDeclaredConstructor().newInstance());
    }

    public boolean removeOperation(Task task){
        return tasks.remove(task);
    }

    public Task getOperation(int i){
        return tasks.get(i);
    }

    public boolean containsOperation(Task task){
        return tasks.contains(task);
    }
}
