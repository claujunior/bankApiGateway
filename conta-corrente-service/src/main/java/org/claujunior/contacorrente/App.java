package org.claujunior.contacorrente;

import org.claujunior.contacorrente.heartbeat.TimeoutBasedFailureDetector;
import org.claujunior.contacorrente.servers.ServerFactory;
import org.claujunior.contacorrente.servers.Starter;
import org.claujunior.contacorrente.servers.InterfaceServer;
import org.claujunior.contacorrente.servers.control.ControlHttpServer;
import org.claujunior.contacorrente.service.Service;

import java.util.List;

public final class App {
    public static void main(String[] args) throws Exception {
        TimeoutBasedFailureDetector<?> detector = new TimeoutBasedFailureDetector<>();
        Service service = Service.getInstance();
        List<InterfaceServer> servers = List.of(
                ServerFactory.create("HTTP", 8081, 300),
                ServerFactory.create("UDP", 9091, 300),
                ServerFactory.create("GRPC", 50052, 300)
        );
        Starter starter = new Starter(servers);
        ControlHttpServer controlHttpServer = new ControlHttpServer(8008,starter,detector);
        Thread.startVirtualThread(controlHttpServer::start);
        starter.start();
        detector.start();
        System.out.println(": HTTP=" + 8081 + ", UDP=" + 9091 + ", gRPC=" + 50052 + ", controle=" + 8005);
    }
}
