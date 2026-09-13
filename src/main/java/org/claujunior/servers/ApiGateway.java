package org.claujunior.servers;

import org.claujunior.servers.udp.clientHandlerUdp;

import java.util.List;

public class ApiGateway {
    List<InterfaceServer> servers;
    public ApiGateway(List<InterfaceServer> servers){
        this.servers=servers;
    }
    public void start(){
        for(InterfaceServer server : servers){
            new Thread(server::start).start();
        }
    }
    public void stop(){
        for(InterfaceServer server : servers){
            server.stop();
        }
    }
}
