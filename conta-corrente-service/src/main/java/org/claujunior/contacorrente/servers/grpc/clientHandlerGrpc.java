package org.claujunior.contacorrente.servers.grpc;

import grpc.CadastroResponse;
import grpc.Cliente;
import grpc.ClienteServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.claujunior.contacorrente.service.Service;
import org.claujunior.contacorrente.service.ServiceException;

import java.math.BigDecimal;

public class clientHandlerGrpc extends ClienteServiceGrpc.ClienteServiceImplBase {
    Service service = Service.getInstance();

    @Override
    public void cadastrarCliente(Cliente request, StreamObserver<CadastroResponse> observer) {
        var response = CadastroResponse.newBuilder();
        try {
            criarCliente(request);
            response.setSucesso(true).setMensagem("Conta criada");
        } catch (ServiceException e) {
            response.setSucesso(false).setMensagem(e.getMessage());
        } catch (Exception e) {
            response.setSucesso(false).setMensagem("Falha ao acessar o banco");
        }
        observer.onNext(response.build());
        observer.onCompleted();
    }

    @Override
    public StreamObserver<Cliente> cadastrarMultiCliente(
            StreamObserver<CadastroResponse> observer) {

        return new StreamObserver<>() {
            private int contasCriadas;
            private int falhas;
            private String primeiraFalha;

            @Override
            public void onNext(Cliente cliente) {
                try {
                    criarCliente(cliente);
                    contasCriadas++;
                } catch (ServiceException e) {
                    registrarFalha(e.getMessage());
                } catch (Exception e) {
                    registrarFalha("Falha ao acessar o banco");
                }
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Stream gRPC interrompido: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                var response = CadastroResponse.newBuilder();
                if (falhas == 0) {
                    response.setSucesso(true)
                            .setMensagem(contasCriadas + " contas criadas");
                } else {
                    response.setSucesso(false)
                            .setMensagem(contasCriadas + " contas criadas e "
                                    + falhas + " falharam: " + primeiraFalha);
                }
                observer.onNext(response.build());
                observer.onCompleted();
            }

            private void registrarFalha(String mensagem) {
                falhas++;
                if (primeiraFalha == null) {
                    primeiraFalha = mensagem;
                }
            }
        };
    }

    private void criarCliente(Cliente request) throws Exception {
        BigDecimal saldo;
        try {
            saldo = new BigDecimal(request.getSaldo());
        } catch (NumberFormatException e) {
            throw new ServiceException(400, "Saldo invalido");
        }
        service.criar(request.getNome(), request.getCPF(), saldo);
    }
}