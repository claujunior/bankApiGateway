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
    private TimeoutBasedFailureDetector<InetAddress> executor = TimeoutBasedFailureDetector.getInstance();
    public clientHandlerUdp(DatagramSocket socket, DatagramPacket packet){
        this.socket = socket;
        this.message = new String(packet.getData());
        this.clientIp=packet.getAddress();
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
            if(partes.length < 2){
                sendResponse(socket, 400, "Use servico;operacao;dados", servico);
                return;
            }

            if(!servico.equals("contaCorrente") && !servico.equals("investimento")){
                sendResponse(socket, 400, "Servico deve ser contaCorrente ou investimento", servico);
                return;
            }
            String operacao = partes[1].trim();
            String nome = "";
            String cpf;
            if (operacao.contains("health")){
                if(servico.contains("investimento")){
                    executor.heartBeatReceived(clientIp,"investimento");
                    clientUDP.request(message,executor.choice("contaCorrente"));
                    //sendResponse(socket, 200,String.valueOf(clientIp) + " funcionando" ,servico);
                }
                if(servico.contains("contaCorrente")){
                    executor.heartBeatReceived(clientIp,"contaCorrente");
                    //sendResponse(socket, 200,String.valueOf(clientIp) + " funcionando" ,servico);
                    clientUDP.request(message,executor.choice("contaCorrente"));
                }
                return;
            }
            if(operacao.equals("criar")){
                if(partes.length != 4){
                    sendResponse(socket, 400, "Use " + servico + ";criar;nome;cpf", servico);
                    return;
                }
                nome = partes[2].trim();
                cpf = partes[3].trim();
                if(nome.isEmpty()){
                    sendResponse(socket, 400, "Nome obrigatorio", servico);
                    return;
                }
            } else if(operacao.equals("saldo")){
                if(partes.length != 3){
                    sendResponse(socket, 400, "Use " + servico + ";saldo;cpf", servico);
                    return;
                }
                cpf = partes[2].trim();
            } else if(operacao.equals("attsaldo")){
                if(partes.length != 3){
                    sendResponse(socket, 400, "Use " + servico + ";attsaldo;cpf", servico);
                    return;
                }
                cpf = partes[2].trim();
            } else if(operacao.equals("resgatar")){
                if(partes.length != 3){
                    sendResponse(socket, 400, "Use " + servico + ";resgatar;cpf", servico);
                    return;
                }
                cpf = partes[2].trim();
            } else if(operacao.equals("guardar")){
                if(partes.length != 3){
                    sendResponse(socket, 400, "Use " + servico + ";guardar;cpf", servico);
                    return;
                }
                cpf = partes[2].trim();
            } else if(operacao.equals("deletar")){
                if(partes.length != 3){
                    sendResponse(socket, 400, "Use " + servico + ";deletar;cpf", servico);
                    return;
                }
                cpf = partes[2].trim();
            } else {
                sendResponse(socket, 405, "Operacao nao permitida", servico);
                return;
            }

            if(cpf.isEmpty()){
                sendResponse(socket, 400, "CPF obrigatorio", servico);
                return;
            }
            if (servico.equals("contaCorrente")){
                clientUDP.request(message,executor.choice("contaCorrente"));
            }
            if(servico.equals("investimento")){
                clientUDP.request(message,executor.choice("investimento"));
            }
            sendResponse(socket, 200, "Mensagem recebida: " + servico + ";" + operacao, servico);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendResponse(DatagramSocket socket, int statusCode, String responseString,String choice) {
        try {
            String response = statusCode + "\n" + responseString;
            byte[] responseBytes = response.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length,
                    executor.choice(choice), 9090);
            System.out.println("Address: " + executor.choice(choice));
            socket.send(responsePacket);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
