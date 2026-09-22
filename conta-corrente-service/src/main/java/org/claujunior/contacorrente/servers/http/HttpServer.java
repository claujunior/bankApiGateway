package org.claujunior.contacorrente.servers.http;

import org.claujunior.contacorrente.servers.InterfaceServer;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer implements InterfaceServer {
    private ServerSocket serverSocket;

    int port;
    int backlog;
    public HttpServer(int port, int backlog){
        this.port = port;
        this.backlog = backlog;
    }

    @Override
    public void start() {
        try  {
            synchronized (this) {
                if(serverSocket != null && !serverSocket.isClosed()){
                    return;
                }
                serverSocket = new ServerSocket(port, backlog);
            }
            System.out.println("HttpServer Started");

            while (!serverSocket.isClosed()) {
                Socket remote = serverSocket.accept();
                Thread.startVirtualThread(new clientHandlerHttp(remote));
            }
        } catch (IOException ex) {
            if(serverSocket != null && !serverSocket.isClosed()){
                System.err.println("Falha no servidor HTTP: " + ex.getMessage());
            }
        }
    }

    @Override
    public synchronized void stop() {
        try {
            if(serverSocket != null){
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Falha ao parar servidor HTTP: " + e.getMessage());
        }
    }

}
