package org.claujunior.contacorrente;

import org.claujunior.contacorrente.heartbeat.TimeoutBasedFailureDetector;
import org.claujunior.contacorrente.servers.ServerFactory;
import org.claujunior.contacorrente.servers.Starter;
import org.claujunior.contacorrente.service.Service;

import java.net.InetAddress;
import java.util.List;

public final class App {
    public static void main(String[] args) throws Exception {
        TimeoutBasedFailureDetector<?> detector = new TimeoutBasedFailureDetector<>();
        detector.start();
        Service service = Service.getInstance();
        new Starter(List.of(
                ServerFactory.create("HTTP", 8081, 50),
                ServerFactory.create("UDP", 9091, 50)
                //ServerFactory.create("GRPC", grpc, 50)
        ))
                .start();
        System.out.println(": HTTP=" + 8081 + ", UDP=" + 9091 + ", gRPC=");
    }
}
