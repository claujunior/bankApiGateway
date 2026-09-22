package org.claujunior.contacorrente;

import org.claujunior.contacorrente.heartbeat.TimeoutBasedFailureDetector;
import org.claujunior.contacorrente.servers.ServerFactory;
import org.claujunior.contacorrente.servers.Starter;
import org.claujunior.contacorrente.service.Service;

import java.util.List;

public final class App {
    public static void main(String[] args) throws Exception {
        TimeoutBasedFailureDetector<?> detector = new TimeoutBasedFailureDetector<>();
        detector.start();
        Service service = Service.getInstance();
        new Starter(List.of(
                ServerFactory.create("HTTP", 8081, 300),
                ServerFactory.create("UDP", 9091, 300),
                ServerFactory.create("GRPC", 50052, 300)
        ))
                .start();
        System.out.println(": HTTP=" + 8081 + ", UDP=" + 9091 + ", gRPC=" + 50052);
    }
}
