package org.claujunior.servers.http;

import org.claujunior.servers.interfaceServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer implements interfaceServer {
    private ServerSocket serverSocket;

    int port;
    int backlog;
    public HttpServer(int port, int backlog){
        this.port = port;
        this.backlog = backlog;
    }

    @Override
    public void start() {

        System.out.println("HttpServer Started");
        try  {
            serverSocket = new ServerSocket(port, backlog);

            while (true) {
                Socket remote = serverSocket.accept();
                Thread.startVirtualThread(new clientHandlerHttp(remote));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void stop(){
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
