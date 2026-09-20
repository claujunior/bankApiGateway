package org.claujunior.investimento.servers.grpc;

/*
import grpc.CadastroResponse;
import grpc.Cliente;
import io.grpc.ServerBuilder;
import grpc.ClienteServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.claujunior.investimento.servers.InterfaceServer;

import java.io.IOException;

public class GrpcServer implements InterfaceServer {

    int port;
    public GrpcServer(int port, int backlog){
        this.port = port;

    }
    @Override
    public void start(){
        try {
            var server = ServerBuilder.forPort(port)
                    .addService(new clientHandlerGrpc())
                    .build();
            server.start();
            System.out.println("GrpcServer Started");
            server.awaitTermination();
        } catch (InterruptedException e) {
            System.err.println("Servidor foi finalizado: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
*/
