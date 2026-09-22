package org.claujunior.investimento;

import org.claujunior.investimento.heartbeat.TimeoutBasedFailureDetector;
import org.claujunior.investimento.servers.ServerFactory;
import org.claujunior.investimento.servers.Starter;
import org.claujunior.investimento.service.Service;

import java.util.List;

public final class App {
    public static void main(String[] args) throws Exception {
        TimeoutBasedFailureDetector<?> detector = new TimeoutBasedFailureDetector<>();
        detector.start();
        Service service = Service.getInstance();
        new Starter(List.of(
                ServerFactory.create("HTTP", 8082, 300),
                ServerFactory.create("UDP", 9092, 300),
                ServerFactory.create("GRPC", 50053, 300)
        ))
                .start();
        System.out.println(": HTTP=" + 8082 + ", UDP=" + 9092 + ", gRPC=" + 50053);
    }
}
