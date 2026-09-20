package org.claujunior.contacorrente.servers;


//import org.claujunior.contacorrente.servers.grpc.GrpcServer;
import org.claujunior.contacorrente.servers.http.HttpServer;
import org.claujunior.contacorrente.servers.udp.UDPServer;

public class ServerFactory {

        public static InterfaceServer create(
                String type,
                int port,
                int backlog) {

            return switch (type.toUpperCase()) {
                case "HTTP" -> new HttpServer(port, backlog);
                case "UDP" -> new UDPServer(port, backlog);
                //case "GRPC" -> new GrpcServer(port,backlog);
                default -> throw new IllegalArgumentException(
                        "Tipo de servidor desconhecido: " + type
                );
            };
        }

}
