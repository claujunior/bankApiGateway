package org.claujunior.servers.http;

import org.claujunior.client.HttpClient;
import org.claujunior.heartbeat.TimeoutBasedFailureDetector;
import org.claujunior.twoPhaseCommit.Coordinator;
import org.claujunior.twoPhaseCommit.TransactionRef;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;


public class clientHandlerHttp implements Runnable{
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
            else {
                if(httpMethod.equals("GET")){
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
                } else if (httpMethod.equals("POST")) {
                    if(recurso.contains("investimento")){
                        if(recurso.contains("criar")){
                            sendResponse1(socket,httpClient.request(recurso,httpMethod,httpVersion,json,"investimento"));
                            return;
                        }
                        if(recurso.contains("transferencia")){
                            String[] xd = recurso.split("/");
                            if(xd.length==6) {
                                String key1 = xd[1] + ":" + xd[3];//"investimento:cpf"
                                String key2 = "contaCorrente:" + xd[4];//"contaCorrente:cpf"
                                String valor = xd[5];
                                TransactionRef transactionRef = new TransactionRef(System.nanoTime());
                                coordinator.begin(transactionRef);
                                coordinator.addKeyToTransaction(transactionRef,key1);
                                coordinator.addKeyToTransaction(transactionRef,key2);
                                sendResponse(socket,200,"Transacao iniciada");
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
                            String[] xd = recurso.split("/");
                            if(xd.length==6) {
                                String key1 = xd[1] + ":" + xd[3];//"contaCorrente:cpf"
                                String key2 = "investimento:" + xd[4];//"investimento:cpf"
                                String valor = xd[5];
                                TransactionRef transactionRef = new TransactionRef(System.nanoTime());
                                coordinator.begin(transactionRef);
                                coordinator.addKeyToTransaction(transactionRef,key1);
                                coordinator.addKeyToTransaction(transactionRef,key2);
                                sendResponse(socket,200,"Transacao iniciada");
                                return;
                            }
                        }
                    }
                    sendResponse(socket,404,"Recurso nao encontrado");
                } else if (httpMethod.equals("PUT")) {
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
                } else if (httpMethod.equals("DELETE")) {
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
                } else {
                    sendResponse(socket, 405, "Method Not Allowed");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendResponse1(Socket socket, String aa){
        try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());) {
            out.write(aa.getBytes());
        }catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public void sendResponse(Socket socket, int statusCode, String responseString) {

        String statusLine;

        String serverHeader = "Server: HttpServer\r\n";

        String contentTypeHeader = "Content-Type: text\r\n";

        try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());) {

            if (statusCode == 200) {

                statusLine = "HTTP/1.1 200 OK" + "\r\n";

                String contentLengthHeader = "Content-Length: " + responseString.length() + "\r\n";

                out.writeBytes(statusLine);

                out.writeBytes(serverHeader);

                out.writeBytes(contentTypeHeader);

                out.writeBytes(contentLengthHeader);

                out.writeBytes("\r\n");

                out.writeBytes(responseString);

            } else if (statusCode == 405) {

                statusLine = "HTTP/1.1 405 Method Not Allowed" + "\r\n";

                out.writeBytes(statusLine);

                out.writeBytes("\r\n");

            } else {

                statusLine = "HTTP/1.1 404 Not Found" + "\r\n";

                out.writeBytes(statusLine);

                out.writeBytes("\r\n");
            }

            out.close();
            socket.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
