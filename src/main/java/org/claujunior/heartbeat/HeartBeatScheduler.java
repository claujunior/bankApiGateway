package org.claujunior.heartbeat;

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

    public void start() {

        scheduledTask = executor.scheduleWithFixedDelay(
                action,
                heartBeatInterval,
                heartBeatInterval,
                TimeUnit.MILLISECONDS
        );
    }
}