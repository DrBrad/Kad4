package unet.kad4.utils;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class SpamThrottle {

    private static final int BURST = 10, PER_SECOND = 2;
    private Map<InetAddress, Integer> hitCounter;
    private AtomicLong lastDecayTime;

    public SpamThrottle(){
        hitCounter = new ConcurrentHashMap<>();
        lastDecayTime = new AtomicLong(System.currentTimeMillis());
    }

    public boolean addAndTest(InetAddress addr) {
        return (saturatingAdd(addr) >= BURST);
    }

    public void remove(InetAddress addr) {
        hitCounter.remove(addr);
    }

    public boolean test(InetAddress addr) {
        return hitCounter.getOrDefault(addr, 0) >= BURST;
    }

    public int calculateDelayAndAdd(InetAddress addr) {
        int counter = hitCounter.compute(addr, (key, old) -> old == null ? 1 : old + 1);
        int diff = counter - BURST;
        return Math.max(diff, 0)*1000/PER_SECOND;
    }

    public void saturatingDec(InetAddress addr) {
        hitCounter.compute(addr, (key, old) -> old == null || old == 1 ? null : old - 1);
    }

    public int saturatingAdd(InetAddress addr) {
        return hitCounter.compute(addr, (key, old) -> old == null ? 1 : Math.min(old + 1, BURST));
    }

    public void decay() {
        long now = System.currentTimeMillis();
        long last = lastDecayTime.get();
        long deltaT = TimeUnit.MILLISECONDS.toSeconds(now - last);
        if(deltaT < 1)
            return;
        if(!lastDecayTime.compareAndSet(last, last + deltaT * 1000))
            return;

        int deltaC = (int) (deltaT * PER_SECOND);

        // minor optimization: delete first, then replace only what's left
        hitCounter.entrySet().removeIf(entry -> entry.getValue() <= deltaC);
        hitCounter.replaceAll((k, v) -> v - deltaC);

    }
}
