package org.claujunior.servers.http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.StringTokenizer;


public class clientHandlerHttp implements Runnable{
    private Socket socket;

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

            StringTokenizer tokenizer = new StringTokenizer(headerLine);

            String httpMethod = tokenizer.nextToken();

            if (httpMethod.equals("GET")) {

                System.out.println("Get method processed");

                String httpQueryString = tokenizer.nextToken();

                StringBuilder responseBuffer = new StringBuilder();

                responseBuffer

                        .append("<html><h1>WebServer Home Page.... </h1><br>")

                        .append("<b>Bem vindo ao Meu web server! </b><BR>")

                        .append("</html>");

                sendResponse(socket, 200, responseBuffer.toString());

            } else {

                System.out.println("The HTTP method is not recognized");

                sendResponse(socket, 405, "Method Not Allowed");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void sendResponse(Socket socket, int status, String response) {

    }
}
