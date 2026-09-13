package org.claujunior.servers;


import org.claujunior.servers.http.HttpServer;
import org.claujunior.servers.udp.UDPServer;

public class serverFactory {

        public static interfaceServer create(
                String type,
                int port,
                int backlog) {

            return switch (type.toUpperCase()) {
                case "HTTP" -> new HttpServer(port, backlog);
                case "UDP" -> new UDPServer(port, backlog);
                default -> throw new IllegalArgumentException(
                        "Tipo de servidor desconhecido: " + type
                );
            };
        }

}
