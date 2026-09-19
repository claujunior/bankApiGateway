package org.claujunior.contacorrente.servers.grpc;

import grpc.CadastroResponse;
import grpc.Cliente;
import grpc.ClienteServiceGrpc;
import io.grpc.stub.StreamObserver;

public class clientHandlerGrpc extends ClienteServiceGrpc.ClienteServiceImplBase {
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
