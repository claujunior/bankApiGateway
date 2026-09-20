package org.claujunior.servers.udp;


import org.claujunior.client.ClientUDP;
import org.claujunior.heartbeat.TimeoutBasedFailureDetector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class clientHandlerUdp implements Runnable{
    private DatagramSocket socket;
    private ClientUDP clientUDP;
    InetAddress clientIp;
    private String message;
    int port;
    private TimeoutBasedFailureDetector<InetAddress> executor = TimeoutBasedFailureDetector.getInstance();
    public clientHandlerUdp(DatagramSocket socket, DatagramPacket packet){
        this.socket = socket;
        this.message = new String(packet.getData());
        this.clientIp=packet.getAddress();
        this.port= packet.getPort();
    }

    @Override
    public void run() {
        handleRequest(socket);
    }

    private void handleRequest(DatagramSocket socket) {
        try {

            message = message.trim();
            String[] partes = message.split(";", -1);
            String servico = partes[0].trim();
            if(servico.contains("resposta")){
                sendResponse(socket,servico);
            }
            if(partes.length < 2){
                sendResponse(socket, "Use servico;operacao;dados");
                return;
            }

            if(!servico.equals("contaCorrente") && !servico.equals("investimento")){
                sendResponse(socket, "Servico deve ser contaCorrente ou investimento");
                return;
            }
            String operacao = partes[1].trim();
            String nome = "";
            String cpf;
            if (operacao.contains("health")){
                if(servico.contains("investimento")){
                    executor.heartBeatReceived(clientIp,"investimento");
                }
                if(servico.contains("contaCorrente")){
                    executor.heartBeatReceived(clientIp,"contaCorrente");
                }
                return;
            }
            if(operacao.equals("criar")){
                if(partes.length != 4){
                    sendResponse(socket, "Use " + servico + ";criar;nome;cpf");
                    return;
                }
                nome = partes[2].trim();
                cpf = partes[3].trim();
                if(nome.isEmpty()){
                    sendResponse(socket, "Nome obrigatorio");
                    return;
                }
            } else if(operacao.equals("saldo")){
                if(partes.length != 3){
                    sendResponse(socket, "Use " + servico + ";saldo;cpf");
                    return;
                }
                cpf = partes[2].trim();
            } else if(operacao.equals("attsaldo")){
                if(partes.length != 3){
                    sendResponse(socket, "Use " + servico + ";attsaldo;cpf");
                    return;
                }
                cpf = partes[2].trim();
            } else if(operacao.equals("resgatar")){
                if(partes.length != 3){
                    sendResponse(socket, "Use " + servico + ";resgatar;cpf");
                    return;
                }
                cpf = partes[2].trim();
            } else if(operacao.equals("guardar")){
                if(partes.length != 3){
                    sendResponse(socket, "Use " + servico + ";guardar;cpf");
                    return;
                }
                cpf = partes[2].trim();
            } else if(operacao.equals("deletar")){
                if(partes.length != 3){
                    sendResponse(socket, "Use " + servico + ";deletar;cpf");
                    return;
                }
                cpf = partes[2].trim();
            } else {
                sendResponse(socket, "Operacao nao permitida");
                return;
            }
            if (servico.equals("contaCorrente")){
                clientUDP.request(message,executor.choice("contaCorrente"));
            }
            if(servico.equals("investimento")){
                clientUDP.request(message,executor.choice("investimento"));
            }
            sendResponse(socket, "Mensagem recebida: " + servico + ";" + operacao);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendResponse(DatagramSocket socket, String responseString) {
        try {
            byte[] responseBytes = responseString.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length,
                    clientIp, port);
            socket.send(responsePacket);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
