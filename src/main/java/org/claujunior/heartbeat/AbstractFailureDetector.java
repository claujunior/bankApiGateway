package org.claujunior.heartbeat;

abstract class AbstractFailureDetector <T>{
    private HeartBeatScheduler heartbeatScheduler = new HeartBeatScheduler(this::heartBeatCheck, 100l);
    public void start() {
        heartbeatScheduler.start();
    }
    abstract void heartBeatCheck();
    abstract void heartBeatReceived(T serverId);

}
