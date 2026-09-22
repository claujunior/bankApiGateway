package org.claujunior.investimento.servers;


import java.util.List;

public class Starter {
    private List<InterfaceServer> servers;
    private boolean running;
    public Starter(List<InterfaceServer> servers){
        this.servers=servers;
    }
    public synchronized void start(){
        if(running){
            return;
        }
        running = true;
        for(InterfaceServer server : servers){
            new Thread(server::start).start();
        }
    }
    public synchronized void stop(){
        if(!running){
            return;
        }
        for(InterfaceServer server : servers){
            server.stop();
        }
        running = false;
    }
    public synchronized boolean isRunning(){
        return running;
    }
}
