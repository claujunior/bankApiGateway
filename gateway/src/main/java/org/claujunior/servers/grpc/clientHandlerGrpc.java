package org.claujunior.servers.grpc;

import grpc.CadastroResponse;
import grpc.Cliente;
import grpc.ClienteServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.claujunior.client.GrpcClient;
import org.claujunior.heartbeat.TimeoutBasedFailureDetector;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class clientHandlerGrpc extends ClienteServiceGrpc.ClienteServiceImplBase {
    TimeoutBasedFailureDetector<InetAddress> executor = TimeoutBasedFailureDetector.getInstance();
    GrpcClient grpcClient = new GrpcClient();

    @Override
    public void cadastrarCliente(Cliente request, StreamObserver<CadastroResponse> responseObserver) {
        InetAddress server = executor.choice(request.getSelecao());
        CadastroResponse response;

        if (server == null) {
            response = errorResponse("Servico indisponivel");
        } else {
            response = grpcClient.request(request, server);
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Cliente> cadastrarMultiCliente(
            StreamObserver<CadastroResponse> responseObserver) {

        return new StreamObserver<>() {
            private final List<Cliente> clientes = new ArrayList<>();
            private String selecao;
            private String erro;

            @Override
            public void onNext(Cliente cliente) {
                if (selecao == null) {
                    selecao = cliente.getSelecao();
                }
                if (!selecao.equals(cliente.getSelecao())) {
                    erro = "Todos os clientes do stream devem usar o mesmo servico";
                    return;
                }
                clientes.add(cliente);
            }

            @Override
            public void onError(Throwable t) {
                clientes.clear();
            }

            @Override
            public void onCompleted() {
                if (clientes.isEmpty()) {
                    sendResponse(responseObserver, false, "Nenhum cliente recebido");
                    return;
                }
                if (erro != null) {
                    sendResponse(responseObserver, false, erro);
                    return;
                }

                InetAddress server = executor.choice(selecao);
                if (server == null) {
                    sendResponse(responseObserver, false, "Servico indisponivel");
                    return;
                }

                grpcClient.requestStream(
                        clientes,
                        selecao,
                        server,
                        responseObserver
                );
            }
        };
    }

    private CadastroResponse errorResponse(String mensagem) {
        return CadastroResponse.newBuilder()
                .setSucesso(false)
                .setMensagem(mensagem)
                .build();
    }

    private void sendResponse(StreamObserver<CadastroResponse> observer,
                              boolean sucesso, String mensagem) {
        observer.onNext(
                CadastroResponse.newBuilder()
                        .setSucesso(sucesso)
                        .setMensagem(mensagem)
                        .build()
        );
        observer.onCompleted();
    }
}