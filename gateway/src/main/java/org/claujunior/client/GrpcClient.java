package org.claujunior.client;

import grpc.CadastroResponse;
import grpc.Cliente;
import grpc.ClienteServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GrpcClient {

    public CadastroResponse request(Cliente request, InetAddress address) {
        ManagedChannel channel = createChannel(request.getSelecao(), address);

        try {
            var stub = ClienteServiceGrpc.newBlockingStub(channel)
                    .withDeadlineAfter(5, TimeUnit.SECONDS);
            return stub.cadastrarCliente(clienteForService(request));
        } catch (StatusRuntimeException e) {
            return errorResponse("Falha ao acessar " + request.getSelecao());
        } finally {
            channel.shutdown();
        }
    }

    public void requestStream(List<Cliente> requests, String selecao,
                              InetAddress address,
                              StreamObserver<CadastroResponse> responseObserver) {
        ManagedChannel channel = createChannel(selecao, address);
        var stub = ClienteServiceGrpc.newStub(channel)
                .withDeadlineAfter(5, TimeUnit.SECONDS);

        StreamObserver<CadastroResponse> serviceResponseObserver =
                new StreamObserver<>() {
                    @Override
                    public void onNext(CadastroResponse response) {
                        responseObserver.onNext(response);
                    }

                    @Override
                    public void onError(Throwable t) {
                        responseObserver.onNext(
                                errorResponse("Falha ao acessar " + selecao)
                        );
                        responseObserver.onCompleted();
                        channel.shutdown();
                    }

                    @Override
                    public void onCompleted() {
                        responseObserver.onCompleted();
                        channel.shutdown();
                    }
                };

        try {
            StreamObserver<Cliente> requestObserver =
                    stub.cadastrarMultiCliente(serviceResponseObserver);

            for (Cliente request : requests) {
                requestObserver.onNext(clienteForService(request));
            }
            requestObserver.onCompleted();
        } catch (Exception e) {
            serviceResponseObserver.onError(e);
        }
    }

    private ManagedChannel createChannel(String selecao, InetAddress address) {
        int port = "investimento".equals(selecao)
                ? Integer.getInteger("investimento.grpc.port", 50053)
                : Integer.getInteger("contacorrente.grpc.port", 50052);

        return ManagedChannelBuilder
                .forAddress(address.getHostAddress(), port)
                .usePlaintext()
                .build();
    }

    private Cliente clienteForService(Cliente request) {
        return Cliente.newBuilder()
                .setNome(request.getNome())
                .setCPF(request.getCPF())
                .setSaldo(request.getSaldo())
                .build();
    }

    private CadastroResponse errorResponse(String mensagem) {
        return CadastroResponse.newBuilder()
                .setSucesso(false)
                .setMensagem(mensagem)
                .build();
    }
}