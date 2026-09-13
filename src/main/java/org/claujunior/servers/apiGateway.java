package org.claujunior.servers;

import java.util.List;

public class apiGateway {
    List<interfaceServer> servers;
    public apiGateway(List<interfaceServer> servers){
        this.servers=servers;
    }
    public void start(){
        for(interfaceServer server : servers){
            server.start();
        }
    }
    public void stop(){
        for(interfaceServer server : servers){
            server.stop();
        }
    }
}
