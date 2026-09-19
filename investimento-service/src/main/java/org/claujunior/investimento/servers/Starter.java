package org.claujunior.investimento.servers;


import java.util.List;

public class Starter {
    private List<InterfaceServer> servers;
    public Starter(List<InterfaceServer> servers){
        this.servers=servers;
    }
    public void start(){
        for(InterfaceServer server : servers){
            new Thread(server::start).start();
        }
    }
}
