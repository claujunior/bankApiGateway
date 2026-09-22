package org.claujunior.servers.http;

import org.claujunior.client.HttpClient;
import org.claujunior.heartbeat.TimeoutBasedFailureDetector;
import org.claujunior.twoPhaseCommit.Coordinator;
import org.claujunior.twoPhaseCommit.ReplicaMapper;
import org.claujunior.twoPhaseCommit.TransactionRef;
import org.claujunior.twoPhaseCommit.TransactionStatus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.math.BigDecimal;
import java.util.StringTokenizer;


public class clientHandlerHttp implements Runnable{
    private ReplicaMapper replicaMapper = new ReplicaMapper();
    private Socket socket;
    private HttpClient httpClient = HttpClient.getInstance();
    private TimeoutBasedFailureDetector<InetAddress> executor = TimeoutBasedFailureDetector.getInstance();
    private Coordinator coordinator = Coordinator.getInstance();
    public clientHandlerHttp(Socket socket){
        this.socket = socket;
    }
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
            char[] bodychar = new char[contentLength];
            int num = in.read(bodychar);
            String json = new String(bodychar,0,num);
            HttpClient httpClient = HttpClient.getInstance();
            if(!httpVersion.equals("HTTP/1.1")){
                sendResponse(socket, 200, "Correct version HTTP/1.1");
                return;
            }
            processRequest(socket, httpClient, httpMethod, recurso, httpVersion, json);
        } catch (Exception e) {
            sendResponse(socket,500,"Erro interno do gateway");
        }
    }

    private void processRequest(Socket socket, HttpClient httpClient, String httpMethod,
                                String recurso, String httpVersion, String json) {
        if(httpMethod.equals("GET")){
            handleGet(socket, httpClient, recurso, httpMethod, httpVersion, json);
        } else if (httpMethod.equals("POST")) {
            handlePost(socket, httpClient, recurso, httpMethod, httpVersion, json);
        } else if (httpMethod.equals("PUT")) {
            handlePut(socket, httpClient, recurso, httpMethod, httpVersion, json);
        } else if (httpMethod.equals("DELETE")) {
            handleDelete(socket, httpClient, recurso, httpMethod, httpVersion, json);
        } else {
            sendResponse(socket, 405, "Method Not Allowed");
        }
    }

    private void handleGet(Socket socket, HttpClient httpClient, String recurso,
                           String httpMethod, String httpVersion, String json) {
        if(handleProcessManager(socket,recurso,httpMethod)){
            return;
        }
        if(recurso.contains("investimento")){
            if(recurso.contains("saldo")){
                sendResponse1(socket,httpClient.request(recurso,httpMethod,httpVersion,json,"investimento"));
                return;
            }
        }
        else if(recurso.contains("contaCorrente")){
            if(recurso.contains("saldo")){
                sendResponse1(socket,httpClient.request(recurso,httpMethod,httpVersion,json,"contaCorrente"));
                return;
            }
        }
        sendResponse(socket,404,"Recurso nao encontrado");
    }

    private void handlePost(Socket socket, HttpClient httpClient, String recurso,
                            String httpMethod, String httpVersion, String json) {
        if(handleProcessManager(socket,recurso,httpMethod)){
            return;
        }
        if(recurso.contains("investimento")){
            if(recurso.contains("criar")){
                sendResponse1(socket,httpClient.request(recurso,httpMethod,httpVersion,json,"investimento"));
                return;
            }
            if(recurso.contains("transferencia")){
                if(handleTransfer(socket, recurso, "contaCorrente")){
                    return;
                }
            }
        }
        else if(recurso.contains("contaCorrente")){
            if(recurso.contains("criar")){
                sendResponse1(socket,httpClient.request(recurso,httpMethod,httpVersion,json,"contaCorrente"));
                return;
            }
            if(recurso.contains("transferencia")){
                if(handleTransfer(socket, recurso, "investimento")){
                    return;
                }
            }
        }
        sendResponse(socket,404,"Recurso nao encontrado");
    }

    private boolean handleProcessManager(Socket socket, String recurso, String httpMethod) {
        String[] partes = recurso.split("/");
        if(partes.length != 3){
            return false;
        }

        String servico = partes[1];
        String operacao = partes[2];
        if(!servico.equals("contaCorrente") && !servico.equals("investimento")){
            return false;
        }
        if(httpMethod.equals("POST") && !operacao.equals("start") && !operacao.equals("stop")){
            return false;
        }
        if(httpMethod.equals("GET") && !operacao.equals("status")){
            return false;
        }

        try {
            String variavel = servico.equals("investimento")
                    ? "INVESTIMENTO_MANAGER_HOST"
                    : "CONTA_CORRENTE_MANAGER_HOST";
            String enderecoPadrao = servico.equals("investimento")
                    ? "172.31.30.181"
                    : "172.31.23.93";
            InetAddress endereco = InetAddress.getByName(
                    System.getenv().getOrDefault(variavel,enderecoPadrao)
            );
            sendResponse1(socket,httpClient.request(
                    "/" + operacao,
                    httpMethod,
                    "HTTP/1.1",
                    "",
                    servico,
                    endereco,
                    8008
            ));
        } catch (Exception e) {
            sendResponse(socket,500,"Erro ao acessar controlador do servico");
        }
        return true;
    }

    private boolean handleTransfer(Socket socket, String recurso, String destino) {
        String[] xd = recurso.split("/");
        if(xd.length==6) {
            String key1 = xd[1] + ":" + xd[3];
            String key2 = destino + ":" + xd[4];
            BigDecimal valorTransferencia;
            try {
                valorTransferencia = new BigDecimal(xd[5]);
            } catch (NumberFormatException e) {
                sendResponse(socket,400,"Valor invalido");
                return true;
            }
            if(valorTransferencia.signum() <= 0){
                sendResponse(socket,400,"Valor deve ser positivo");
                return true;
            }
            String valor = valorTransferencia.toPlainString();
            TransactionRef transactionRef = new TransactionRef(System.nanoTime());
            coordinator.begin(transactionRef);
            coordinator.addKeyToTransaction(transactionRef,key1);
            coordinator.addKeyToTransaction(transactionRef,key2);

            InetAddress server1 = replicaMapper.serverFor(key1);
            InetAddress server2 = replicaMapper.serverFor(key2);
            if(server1 != null && server2 != null){
                coordinator.addServerToTransaction(transactionRef,key1,server1);
                coordinator.addServerToTransaction(transactionRef,key2,server2);
                String transactionId = transactionRef.getTxnId().toString();
                String response1 = httpClient.request(
                        "/transaction/put/" + transactionId + "/" + key1 + "/-" + valor,
                        "PUT","HTTP/1.1","",xd[1],server1
                );
                String response2 = httpClient.request(
                        "/transaction/put/" + transactionId + "/" + key2 + "/" + valor,
                        "PUT","HTTP/1.1","",destino,server2
                );
                if(isSuccessful(response1) && isSuccessful(response2)){
                    coordinator.setStatus(transactionRef, TransactionStatus.PREPARING);
                    String prepare1 = requestTransaction("prepare",transactionId,xd[1],server1);
                    String prepare2 = requestTransaction("prepare",transactionId,destino,server2);
                    if(isSuccessful(prepare1) && isSuccessful(prepare2)){
                        coordinator.setStatus(transactionRef, TransactionStatus.PREPARED);
                        coordinator.setStatus(transactionRef, TransactionStatus.COMMITTED);
                        if(commitTransaction(transactionId,xd[1],destino,server1,server2)){
                            sendResponse(socket,200,"Transferencia realizada");
                        } else {
                            sendResponse(socket,500,"Transacao confirmada, mas existe commit pendente");
                        }
                    } else {
                        abortTransaction(transactionRef,transactionId,xd[1],destino,server1,server2);
                        sendResponse(socket,409,errorFromResponses(
                                "Transacao cancelada",prepare1,prepare2
                        ));
                    }
                } else {
                    abortTransaction(transactionRef,transactionId,xd[1],destino,server1,server2);
                    sendResponse(socket,409,errorFromResponses(
                            "Erro ao iniciar transacao",response1,response2
                    ));
                }
            } else {
                sendResponse(socket,404,"Usuario nao encontrado");
            }
            return true;
        }
        return false;
    }

    private boolean commitTransaction(String transactionId, String servico1,
                                      String servico2, InetAddress server1,
                                      InetAddress server2) {
        boolean commit1 = false;
        boolean commit2 = false;

        for(int tentativa = 0; tentativa < 3 && (!commit1 || !commit2); tentativa++){
            if(!commit1){
                commit1 = isSuccessful(
                        requestTransaction("commit",transactionId,servico1,server1)
                );
            }
            if(!commit2){
                commit2 = isSuccessful(
                        requestTransaction("commit",transactionId,servico2,server2)
                );
            }
        }
        return commit1 && commit2;
    }

    private boolean isSuccessful(String response) {
        int bodyStart = response.indexOf("\r\n\r\n");
        if(bodyStart == -1){
            return false;
        }
        return response.substring(bodyStart + 4).trim().equals("true");
    }

    private String errorFromResponses(String defaultMessage, String... responses) {
        for(String response : responses){
            int bodyStart = response.indexOf("\r\n\r\n");
            if(bodyStart != -1){
                String body = response.substring(bodyStart + 4).trim();
                if(!body.isEmpty() && !body.equals("true")){
                    return body;
                }
            }
        }
        return defaultMessage;
    }

    private String requestTransaction(String operacao, String transactionId,
                                      String servico, InetAddress server) {
        return httpClient.request(
                "/transaction/" + operacao + "/" + transactionId,
                "PUT","HTTP/1.1","",servico,server
        );
    }

    private void abortTransaction(TransactionRef transactionRef, String transactionId,
                                  String servico1, String servico2,
                                  InetAddress server1, InetAddress server2) {
        requestTransaction("abort",transactionId,servico1,server1);
        requestTransaction("abort",transactionId,servico2,server2);
        coordinator.setStatus(transactionRef, TransactionStatus.ROLLED_BACK);
    }

    private void handlePut(Socket socket, HttpClient httpClient, String recurso,
                           String httpMethod, String httpVersion, String json) {
        if(recurso.contains("investimento")){
            if(recurso.contains("att") || recurso.contains("guardar") || recurso.contains("resgatar")){
                sendResponse1(socket,httpClient.request(recurso,httpMethod,httpVersion,json,"investimento"));
                return;
            }
        }
        else if(recurso.contains("contaCorrente")){
            if(recurso.contains("att") || recurso.contains("guardar") || recurso.contains("resgatar")){
                sendResponse1(socket,httpClient.request(recurso,httpMethod,httpVersion,json,"contaCorrente"));
                return;
            }
        }
        sendResponse(socket,404,"Recurso nao encontrado");
    }

    private void handleDelete(Socket socket, HttpClient httpClient, String recurso,
                              String httpMethod, String httpVersion, String json) {
        if(recurso.contains("investimento")){
            if(recurso.contains("deletar")){
                sendResponse1(socket,httpClient.request(recurso,httpMethod,httpVersion,json,"investimento"));
                return;
            }
        }
        else if(recurso.contains("contaCorrente")){
            if(recurso.contains("deletar")){
                sendResponse1(socket,httpClient.request(recurso,httpMethod,httpVersion,json,"contaCorrente"));
                return;
            }
        }
        sendResponse(socket,404,"Recurso nao encontrado");
    }

    public void sendResponse1(Socket socket, String aa){
        if(aa==null){
            sendResponse(socket,500,"servidor indisponivel");
            return;
        }
        try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());) {
            out.write(aa.getBytes());
        }catch (IOException ex) {
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
