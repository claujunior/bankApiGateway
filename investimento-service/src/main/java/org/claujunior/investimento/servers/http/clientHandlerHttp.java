package org.claujunior.investimento.servers.http;

import org.claujunior.investimento.service.Service;
import org.claujunior.investimento.service.ServiceException;
import org.claujunior.investimento.twoPhaseCommit.TransactionRef;
import org.claujunior.investimento.twoPhaseCommit.TransactionalKVStore;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.SQLException;
import java.util.StringTokenizer;


public class clientHandlerHttp implements Runnable{
    private Socket socket;
    public clientHandlerHttp(Socket socket){
        this.socket = socket;
    }
    private Service service = Service.getInstance();
    private TransactionalKVStore transactionalKVStore = TransactionalKVStore.getInstance();
    @Override
    public void run() {
        handleRequest(socket);
    }

    private void handleRequest(Socket socket) {
        try (BufferedReader in = new BufferedReader(

                new InputStreamReader(socket.getInputStream()));) {

            String headerLine = in.readLine();
            String clientIp = socket.getInetAddress().getHostAddress();
            StringTokenizer tokenizer = new StringTokenizer(headerLine);

            String httpMethod = tokenizer.nextToken();
            String recurso = tokenizer.nextToken();
            String httpVersion = tokenizer.nextToken();
            httpVersion=httpVersion.toUpperCase();
            int contentLength = 0;
            while (!(headerLine = in.readLine()).isEmpty()) {
                if (headerLine.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(
                            headerLine.substring("Content-Length:".length()).trim()
                    );
                }
            }
            if(!httpVersion.equals("HTTP/1.1")){
                sendResponse(socket, 200, "Correct version HTTP/1.1");
            }
            processRequest(socket, recurso);

        } catch (ServiceException e) {
            sendResponse(socket,e.status(),e.getMessage());
        } catch (Exception e) {
            sendResponse(socket,500,"Erro interno do servico");
        }
    }

    private void processRequest(Socket socket, String recurso) throws SQLException {
        String[] split = recurso.split("/");
        handleCriar(socket, recurso, split);
        handleSaldo(socket, recurso, split);
        handleDeletarTudo(socket, recurso, split);
        handleDeletar(socket, recurso, split);
        handleAtualizar(socket, recurso, split);
        handleGuardar(socket, recurso, split);
        handleResgatar(socket, recurso, split);
        handleTransactionPut(socket, recurso, split);
        handleTransactionPrepare(socket, recurso, split);
        handleTransactionCommit(socket, recurso, split);
        handleTransactionAbort(socket, recurso, split);
    }

    private void handleCriar(Socket socket, String recurso, String[] split) throws SQLException {
        if(recurso.contains("criar")){
            if(split.length==5){
                String[] dados  = {split[3], split[4]};
                sendResponse(socket,200,service.executar(split[2],dados));
            }
        }
    }

    private void handleSaldo(Socket socket, String recurso, String[] split) throws SQLException {
        if(recurso.contains("saldo")){
            if(split.length==4){
                String[] dados  = {split[3]};
                sendResponse(socket,200,service.executar(split[2],dados));
            }
            if(split.length==5 && split[4].equals("transacao")){
                String[] dados  = {split[3]};
                sendResponse(socket,200,"true;" + service.executar(split[2],dados));
            }
        }
    }

    private void handleDeletar(Socket socket, String recurso, String[] split) throws SQLException {
        if(recurso.contains("deletar")){
            if(split.length==4){
                String[] dados  = {split[3]};
                sendResponse(socket,200,service.executar(split[2],dados));
            }
        }
    }

    private void handleDeletarTudo(Socket socket, String recurso, String[] split) throws SQLException {
        if(recurso.contains("deletarTudo") && split.length==3){
            int quantidade = service.deletarTudo();
            sendResponse(socket,200,quantidade + " contas removidas");
        }
    }

    private void handleAtualizar(Socket socket, String recurso, String[] split) throws SQLException {
        if(recurso.contains("att")){
            if(split.length==5){
                String[] dados  = {split[3],split[4]};
                sendResponse(socket,200,service.executar(split[2],dados));
            }
        }
    }

    private void handleGuardar(Socket socket, String recurso, String[] split) throws SQLException {
        if(recurso.contains("guardar")){
            if(split.length==5){
                String[] dados  = {split[3],split[4]};
                sendResponse(socket,200,service.executar(split[2],dados));
            }
        }
    }

    private void handleResgatar(Socket socket, String recurso, String[] split) throws SQLException {
        if(recurso.contains("resgatar")){
            if(split.length==5){
                String[] dados  = {split[3],split[4]};
                sendResponse(socket,200,service.executar(split[2],dados));
            }
        }
    }

    private void handleTransactionPut(Socket socket, String recurso, String[] split) {
        if(recurso.contains("transaction/put")){
            if(split.length==6){
                TransactionRef transactionRef = new TransactionRef(split[3]);
                boolean sucesso = transactionalKVStore.put(transactionRef,split[4],split[5]);
                sendTransactionResponse(socket,sucesso,"Transacao nao aceita");
            }
        }
    }

    private void handleTransactionPrepare(Socket socket, String recurso, String[] split) {
        if(recurso.contains("transaction/prepare")){
            if(split.length==4){
                TransactionRef transactionRef = new TransactionRef(split[3]);
                boolean sucesso = transactionalKVStore.prepare(transactionRef);
                sendTransactionResponse(socket,sucesso,"Transacao nao preparada");
            }
        }
    }

    private void handleTransactionCommit(Socket socket, String recurso, String[] split) {
        if(recurso.contains("transaction/commit")){
            if(split.length==4){
                TransactionRef transactionRef = new TransactionRef(split[3]);
                boolean sucesso = transactionalKVStore.commit(transactionRef);
                sendTransactionResponse(socket,sucesso,"Commit nao realizado");
            }
        }
    }

    private void handleTransactionAbort(Socket socket, String recurso, String[] split) {
        if(recurso.contains("transaction/abort")){
            if(split.length==4){
                TransactionRef transactionRef = new TransactionRef(split[3]);
                boolean sucesso = transactionalKVStore.abort(transactionRef);
                sendTransactionResponse(socket,sucesso,"Abort nao realizado");
            }
        }
    }

    private void sendTransactionResponse(Socket socket, boolean sucesso, String erro) {
        if(sucesso){
            sendResponse(socket,200,"true");
        } else {
            sendResponse(socket,409,"false;" + erro);
        }
    }

    public void sendResponse(Socket socket, int statusCode, String responseString) {

        String statusLine;

        String serverHeader = "Server: HttpServer\r\n";

        String contentTypeHeader = "Content-Type: text\r\n";

        try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());) {

            statusLine = "HTTP/1.1 " + statusCode + " " + statusText(statusCode) + "\r\n";
            String contentLengthHeader = "Content-Length: " + responseString.length() + "\r\n";
            out.writeBytes(statusLine);
            out.writeBytes(serverHeader);
            out.writeBytes(contentTypeHeader);
            out.writeBytes(contentLengthHeader);
            out.writeBytes("\r\n");
            out.writeBytes(responseString);

            out.close();
            socket.close();

        } catch (IOException ex) {
        }

    }

    private String statusText(int statusCode) {
        if(statusCode == 200) return "OK";
        if(statusCode == 400) return "Bad Request";
        if(statusCode == 404) return "Not Found";
        if(statusCode == 405) return "Method Not Allowed";
        if(statusCode == 409) return "Conflict";
        return "Internal Server Error";
    }
}
