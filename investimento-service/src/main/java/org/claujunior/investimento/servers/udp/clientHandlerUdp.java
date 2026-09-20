package org.claujunior.investimento.servers.udp;

import org.claujunior.investimento.service.Service;
import org.claujunior.investimento.service.ServiceException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class clientHandlerUdp implements Runnable{
    private DatagramSocket socket;
    private final InetAddress clientIp;
    private final int clientPort;
    private String message;
    public clientHandlerUdp(DatagramSocket socket, DatagramPacket packet){
        this.socket = socket;
        this.clientIp = packet.getAddress();
        this.clientPort = packet.getPort();
        this.message = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }
    private Service service = Service.getInstance();

    @Override
    public void run() {
        handleRequest(socket);
    }

    private void handleRequest(DatagramSocket socket) {
        try {
            message = message.trim();
            String[] split = message.split(";");

            if(message.contains("criar")){
                if(split.length==4){
                    String[] dados = {split[2], split[3]};
                    String response = service.executar(split[1],dados);
                    sendResponse(socket, 200, response);
                }
            }
            if(message.contains("saldo")){
                if(split.length==3){
                    String[] dados = {split[2]};
                    String response = service.executar(split[1],dados);
                    sendResponse(socket, 200, response);
                }
            }
            if(message.contains("deletar")){
                if(split.length==3){
                    String[] dados = {split[2]};
                    String response = service.executar(split[1],dados);
                    sendResponse(socket, 200, response);
                }
            }
            if(message.contains("att")){
                if(split.length==4){
                    String[] dados = {split[2],split[3]};
                    String response = service.executar(split[1],dados);
                    sendResponse(socket, 200, response);
                }
            }
            if(message.contains("guardar")){
                if(split.length==4){
                    String[] dados = {split[2],split[3]};
                    String response = service.executar(split[1],dados);
                    sendResponse(socket, 200, response);
                }
            }
            if(message.contains("resgatar")){
                if(split.length==4){
                    String[] dados = {split[2],split[3]};
                    String response = service.executar(split[1],dados);
                    sendResponse(socket, 200, response);
                }
            }
        } catch (ServiceException e) {
            sendResponse(socket, e.status(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(socket, 500, "Falha ao acessar o banco");
        }
    }

    public void sendResponse(DatagramSocket socket, int statusCode, String responseString) {
        try {
            byte[] bytes = (statusCode + "\n" + responseString).getBytes(StandardCharsets.UTF_8);
            socket.send(new DatagramPacket(bytes, bytes.length, clientIp, clientPort));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
