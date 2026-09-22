package org.claujunior.investimento.heartbeat;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HeartBeatScheduler {

    private ScheduledThreadPoolExecutor executor =
            new ScheduledThreadPoolExecutor(1);

    private Runnable action;

    private Long heartBeatInterval;

    public HeartBeatScheduler(
            Runnable action,
            Long heartBeatIntervalMs
    ) {
        this.action = action;
        this.heartBeatInterval = heartBeatIntervalMs;
    }

    private ScheduledFuture scheduledTask;

    public synchronized void start() {

        if(scheduledTask != null && !scheduledTask.isDone()){
            return;
        }

        scheduledTask = executor.scheduleWithFixedDelay(
                action,
                heartBeatInterval,
                heartBeatInterval,
                TimeUnit.MILLISECONDS
        );
    }

    public synchronized void stop() {
        if(scheduledTask != null){
            scheduledTask.cancel(false);
        }
    }
}
