package org.claujunior.contacorrente.servers.control;

import org.claujunior.contacorrente.heartbeat.TimeoutBasedFailureDetector;
import org.claujunior.contacorrente.servers.InterfaceServer;
import org.claujunior.contacorrente.servers.Starter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class ControlHttpServer implements InterfaceServer {
    private final int port;
    private final Starter starter;
    private final TimeoutBasedFailureDetector<?> detector;
    private ServerSocket serverSocket;

    public ControlHttpServer(int port, Starter starter, TimeoutBasedFailureDetector<?> detector) {
        this.port = port;
        this.starter = starter;
        this.detector = detector;
    }

    @Override
    public void start() {
        try {
            serverSocket = new ServerSocket(port, 50);
            System.out.println("ControlHttpServer Started: " + port);
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                Thread.startVirtualThread(() -> handle(socket));
            }
        } catch (Exception e) {
            if(serverSocket != null && !serverSocket.isClosed()){
                System.err.println("Falha no servidor de controle: " + e.getMessage());
            }
        }
    }

    private void handle(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String requestLine = in.readLine();
            if(requestLine == null || requestLine.isBlank()){
                sendResponse(socket,400,"Requisicao invalida");
                return;
            }
            StringTokenizer tokenizer = new StringTokenizer(requestLine);
            String method = tokenizer.nextToken();
            String path = tokenizer.nextToken();

            if(method.equals("POST") && path.equals("/start")){
                starter.start();
                detector.start();
                sendResponse(socket,200,"Conta corrente iniciada");
            } else if(method.equals("POST") && path.equals("/stop")){
                detector.stop();
                starter.stop();
                sendResponse(socket,200,"Conta corrente parada");
            } else if(method.equals("GET") && path.equals("/status")){
                sendResponse(socket,200,starter.isRunning() ? "rodando" : "parado");
            } else {
                sendResponse(socket,404,"Recurso nao encontrado");
            }
        } catch (Exception e) {
            sendResponse(socket,500,"Erro no servidor de controle");
        } finally {
            try {
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }

    private void sendResponse(Socket socket, int statusCode, String response) {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeBytes("HTTP/1.1 " + statusCode + " " + statusText(statusCode) + "\r\n");
            out.writeBytes("Content-Type: text\r\n");
            out.writeBytes("Content-Length: " + response.length() + "\r\n\r\n");
            out.writeBytes(response);
            out.flush();
        } catch (Exception ignored) {
        }
    }

    private String statusText(int statusCode) {
        if(statusCode == 200) return "OK";
        if(statusCode == 400) return "Bad Request";
        if(statusCode == 404) return "Not Found";
        return "Internal Server Error";
    }

    @Override
    public void stop() {
        try {
            if(serverSocket != null){
                serverSocket.close();
            }
        } catch (Exception e) {
            System.err.println("Falha ao parar servidor de controle: " + e.getMessage());
        }
    }
}
