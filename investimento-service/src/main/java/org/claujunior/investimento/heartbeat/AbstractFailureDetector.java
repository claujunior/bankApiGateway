package org.claujunior.investimento.heartbeat;

abstract class AbstractFailureDetector<T>{
    private HeartBeatScheduler heartbeatScheduler = new HeartBeatScheduler(this::heartBeatSend, 100L);
    public void start() {
        heartbeatScheduler.start();
    }
    abstract void heartBeatSend();
}


