package org.claujunior.servers;

import org.claujunior.heartbeat.TimeoutBasedFailureDetector;
import org.claujunior.servers.udp.clientHandlerUdp;

import java.util.List;

public class ApiGateway {
    private TimeoutBasedFailureDetector<String> executor = TimeoutBasedFailureDetector.getInstance();
    private List<InterfaceServer> servers;
    public ApiGateway(List<InterfaceServer> servers){
        this.servers=servers;
    }
    public void start(){
        for(InterfaceServer server : servers){
            new Thread(server::start).start();
        }
        executor.start();
    }
    public void stop(){
        for(InterfaceServer server : servers){
            server.stop();
        }
    }
}
