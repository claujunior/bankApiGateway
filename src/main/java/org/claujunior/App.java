package org.claujunior;


import org.claujunior.servers.ApiGateway;
import org.claujunior.servers.InterfaceServer;
import org.claujunior.servers.ServerFactory;

import java.util.List;

public class App
{
    public static void main( String[] args )
    {
        InterfaceServer http = ServerFactory.create("HTTP", 8080, 300);
        InterfaceServer udp = ServerFactory.create("UDP", 8081, 300);

        ApiGateway apiGateway = new ApiGateway(List.of(http, udp));
        apiGateway.start();
    }
}
