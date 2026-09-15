package org.claujunior.heartbeat;

import org.claujunior.client.HttpClient;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TimeoutBasedFailureDetector <T> extends AbstractFailureDetector<T>{
    private static final TimeoutBasedFailureDetector <String> instance = new TimeoutBasedFailureDetector <String>(10000000000l);



    public static TimeoutBasedFailureDetector <String> getInstance() {
        return instance;
    }

    private final long timeoutNanos;

    private TimeoutBasedFailureDetector(long timeoutNanos) {
        this.timeoutNanos = timeoutNanos;
    }

    private final Map<T, Long> heartbeatReceivedTimes =
            new ConcurrentHashMap<>();

    @Override
    void heartBeatCheck() {
        Long now = System.nanoTime();
        Set<T> serverIds = heartbeatReceivedTimes.keySet();
        for (T serverId : serverIds) {
            Long lastHeartbeatReceivedTime = heartbeatReceivedTimes.get(serverId);
            Long timeSinceLastHeartbeat = now - lastHeartbeatReceivedTime;
            if (timeSinceLastHeartbeat >= timeoutNanos) {
                heartbeatReceivedTimes.remove(serverId);
                markDown(serverId);
            }
        }
    }

    @Override
    public void heartBeatReceived(T serverId) {
        Long currentTime = System.nanoTime();
        heartbeatReceivedTimes.put(serverId, currentTime);
        markUp(serverId);
    }
    private void markDown(T serverId) {
        System.out.println(
                "Servidor " + serverId + " caiu"
        );
    }
    private void markUp(T serverId) {
        System.out.println(
                "Servidor " + serverId + " em pe"
        );
    }

}
