package org.claujunior.investimento.servers.http;

import org.claujunior.investimento.service.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.StringTokenizer;


public class clientHandlerHttp implements Runnable{
    private Socket socket;
    public clientHandlerHttp(Socket socket){
        this.socket = socket;
    }
    private Service service = Service.getInstance();
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
            String[] split = recurso.split("/");
            if(recurso.contains("criar")){
                if(split.length==5){
                String[] dados  = {split[3], split[4]};
                sendResponse(socket,200,service.executar(split[2],dados));
                }
            }
            if(recurso.contains("saldo")){
                if(split.length==4){
                    String[] dados  = {split[3]};
                    sendResponse(socket,200,service.executar(split[2],dados));
                }
            }
            if(recurso.contains("deletar")){
                if(split.length==4){
                    String[] dados  = {split[3]};
                    sendResponse(socket,200,service.executar(split[2],dados));
                }
            }
            if(recurso.contains("att")){
                if(split.length==5){
                    String[] dados  = {split[3],split[4]};
                    sendResponse(socket,200,service.executar(split[2],dados));
                }
            }
            if(recurso.contains("guardar")){
                if(split.length==5){
                    String[] dados  = {split[3],split[4]};
                    sendResponse(socket,200,service.executar(split[2],dados));
                }
            }
            if(recurso.contains("resgatar")){
                if(split.length==5){
                    String[] dados  = {split[3],split[4]};
                    sendResponse(socket,200,service.executar(split[2],dados));
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
