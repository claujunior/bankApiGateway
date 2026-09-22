package org.claujunior.investimento;

import org.claujunior.investimento.heartbeat.TimeoutBasedFailureDetector;
import org.claujunior.investimento.servers.ServerFactory;
import org.claujunior.investimento.servers.Starter;
import org.claujunior.investimento.servers.InterfaceServer;
import org.claujunior.investimento.servers.control.ControlHttpServer;
import org.claujunior.investimento.service.Service;

import java.util.List;

public final class App {
    public static void main(String[] args) throws Exception {
        TimeoutBasedFailureDetector<?> detector = new TimeoutBasedFailureDetector<>();
        Service service = Service.getInstance();
        List<InterfaceServer> servers = List.of(
                ServerFactory.create("HTTP", 8082, 300),
                ServerFactory.create("UDP", 9092, 300),
                ServerFactory.create("GRPC", 50053, 300)
        );
        Starter starter = new Starter(servers);
        ControlHttpServer controlHttpServer = new ControlHttpServer(8008,starter,detector);
        Thread.startVirtualThread(controlHttpServer::start);
        starter.start();
        detector.start();
        System.out.println(": HTTP=" + 8082 + ", UDP=" + 9092 + ", gRPC=" + 50053 + ", controle=" + 8005);
    }
}
