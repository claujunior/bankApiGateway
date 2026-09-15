package org.claujunior.servers.grpc;


import grpc.CadastroResponse;
import grpc.Cliente;
import io.grpc.ServerBuilder;
import grpc.ClienteServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class GrpcServer extends ClienteServiceGrpc.ClienteServiceImplBase {

    public void start(){
        try {
            var server = ServerBuilder.forPort(50052)
                    .addService(new GrpcServer())
                    .build();
            server.start();
            System.out.println("Servidor iniciado na porta 50052");
            server.awaitTermination();
        } catch (InterruptedException e) {
            System.err.println("Servidor foi finalizado: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Override
    public void cadastrarCliente(Cliente request, StreamObserver<CadastroResponse> responseObserver) {
        var cadastrorealizado = CadastroResponse.newBuilder()
                .setMensagem("Cliente " + request.getNome() + " cadastrado com sucesso!")
                .setSucesso(true)
                .build();
        System.out.println("Cliente cadastrado: " + request.getNome() + ", Idade: " + request.getIdade());
        responseObserver.onNext(cadastrorealizado);
        responseObserver.onCompleted();
    }
}
