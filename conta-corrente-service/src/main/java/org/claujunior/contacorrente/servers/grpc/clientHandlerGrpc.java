package org.claujunior.contacorrente.servers.grpc;

//import grpc.CadastroResponse;
//import grpc.Cliente;
//import grpc.ClienteServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.claujunior.contacorrente.service.Service;
import org.claujunior.contacorrente.service.ServiceException;
import java.math.BigDecimal;
/*
public class clientHandlerGrpc extends ClienteServiceGrpc.ClienteServiceImplBase {
    @Override
    public void cadastrarCliente(Cliente request, StreamObserver<CadastroResponse> observer) {
        var response = CadastroResponse.newBuilder();
        try {
            if (!Double.isFinite(request.getSaldo()))
                throw new ServiceException(400, "Saldo invalido");
            Service.getInstance().criar(request.getNome(), request.getCPF(), BigDecimal.valueOf(request.getSaldo()));
            response.setSucesso(true).setMensagem("Conta criada");
        } catch (ServiceException e) {
            response.setSucesso(false).setMensagem(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.setSucesso(false).setMensagem("Falha ao acessar o banco");
        }
        observer.onNext(response.build());
        observer.onCompleted();
    }
}
 */
