package org.claujunior.investimento.servers.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.claujunior.investimento.servers.InterfaceServer;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GrpcServer implements InterfaceServer {

    int port;
    public GrpcServer(int port, int backlog){
        this.port = port;

    }
    @Override
    public void start(){
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Server server = ServerBuilder.forPort(port)
                    .executor(executor)
                    .addService(new clientHandlerGrpc())
                    .build();
            server.start();
            System.out.println("GrpcServer Started");
            server.awaitTermination();
        } catch (InterruptedException e) {
            System.err.println("Servidor foi finalizado: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.err.println("Falha no servidor gRPC: " + e.getMessage());
        }
    }
}
