package org.claujunior.investimento.servers;


import org.claujunior.investimento.servers.grpc.GrpcServer;
import org.claujunior.investimento.servers.http.HttpServer;
import org.claujunior.investimento.servers.udp.UDPServer;

public class ServerFactory {

        public static InterfaceServer create(
                String type,
                int port,
                int backlog) {

            return switch (type.toUpperCase()) {
                case "HTTP" -> new HttpServer(port, backlog);
                case "UDP" -> new UDPServer(port, backlog);
                case "GRPC" -> new GrpcServer(port,backlog);
                default -> throw new IllegalArgumentException(
                        "Tipo de servidor desconhecido: " + type
                );
            };
        }

}
