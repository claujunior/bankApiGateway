package org.claujunior.investimento.servers.udp;

import org.claujunior.investimento.servers.InterfaceServer;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer implements InterfaceServer {
    private DatagramSocket serverSocket;
    int port;
    int backlog;
    public UDPServer(int port, int backlog) {
        this.port = port;
        this.backlog = backlog;
    }

    @Override
    public void start() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            serverSocket = socket;
            System.out.println("UDP Server Started");
            while (!serverSocket.isClosed()) {
                byte[] receiveMessage = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
                serverSocket.receive(receivePacket);
                Thread.startVirtualThread(new clientHandlerUdp(serverSocket, receivePacket));
            }
        } catch (IOException e) {
            if(serverSocket != null && !serverSocket.isClosed()){
                System.err.println("Falha no servidor UDP: " + e.getMessage());
            }
        }
    }

    @Override
    public synchronized void stop() {
        if(serverSocket != null){
            serverSocket.close();
        }
    }
}
