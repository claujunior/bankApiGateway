package org.claujunior.servers.udp;

import org.claujunior.servers.InterfaceServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer implements InterfaceServer {
    private DatagramSocket serverSocket;
    int port;
    int backlog;
    public UDPServer(int port, int backlog){
        this.port = port;
        this.backlog = backlog;
    }

    @Override
    public void start(){
        System.out.println("UDP Server Started");
        try {
            serverSocket = new DatagramSocket(port);
            while (true) {
                byte[] receiveMessage = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
                serverSocket.receive(receivePacket);
                String message = new String(receivePacket.getData());
                Thread.startVirtualThread(new clientHandlerUdp(message));
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }


}
