package org.claujunior.servers.http;

import org.claujunior.client.HttpClient;
import org.claujunior.heartbeat.TimeoutBasedFailureDetector;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.StringTokenizer;


public class clientHandlerHttp implements Runnable{
    private Socket socket;
    private HttpClient httpClient = HttpClient.getInstance();
    private TimeoutBasedFailureDetector<InetAddress> executor = TimeoutBasedFailureDetector.getInstance();
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
            }
            else {
                if(httpMethod.equals("GET")){
                    if(recurso.contains("investimento")){
                        if(recurso.contains("saldo")){
                            httpClient.request(recurso,httpMethod,httpVersion,json,"investimento");
                        }
                    }
                    else if(recurso.contains("contaCorrente")){
                        if(recurso.contains("saldo")){
                            httpClient.request(recurso,httpMethod,httpVersion,json,"contaCorrente");
                        }
                    }
                    sendResponse(socket,200,"mande outra");
                } else if (httpMethod.equals("POST")) {
                    if(recurso.contains("investimento")){
                        if(recurso.contains("criar")){
                            httpClient.request(recurso,httpMethod,httpVersion,json,"investimento");
                        }
                    }
                    else if(recurso.contains("contaCorrente")){
                        if(recurso.contains("criar")){
                            httpClient.request(recurso,httpMethod,httpVersion,json,"contaCorrente");
                        }
                    }
                    sendResponse(socket, 200, "POST");
                } else if (httpMethod.equals("PUT")) {
                    if(recurso.contains("investimento")){
                        if(recurso.contains("resgatar")){
                            httpClient.request(recurso,httpMethod,httpVersion,json,"investimento");
                        }
                        if(recurso.contains("guardar")){
                            httpClient.request(recurso,httpMethod,httpVersion,json,"investimento");
                        }
                    }
                    else if(recurso.contains("contaCorrente")){
                        if(recurso.contains("attsaldo")){
                            httpClient.request(recurso,httpMethod,httpVersion,json,"contaCorrente");
                        }
                    }
                    sendResponse(socket, 200, "PUT");
                } else if (httpMethod.equals("DELETE")) {
                    if(recurso.contains("investimento")){
                        if(recurso.contains("deletar")){
                            httpClient.request(recurso,httpMethod,httpVersion,json,"investimento");
                        }
                    }
                    else if(recurso.contains("contaCorrente")){
                        if(recurso.contains("deletar")){
                            httpClient.request(recurso,httpMethod,httpVersion,json,"contaCorrente");
                        }
                    }
                    sendResponse(socket, 200, "DELETE");
                } else {
                    sendResponse(socket, 405, "Method Not Allowed");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
