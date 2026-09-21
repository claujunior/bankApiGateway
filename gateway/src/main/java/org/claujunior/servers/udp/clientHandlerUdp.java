package org.claujunior.servers.udp;


import org.claujunior.client.ClientUDP;
import org.claujunior.heartbeat.TimeoutBasedFailureDetector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class clientHandlerUdp implements Runnable{
    private DatagramSocket socket;
    private ClientUDP clientUDP = new ClientUDP();
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
            processMessage(socket, partes, servico);
        } catch (Exception e) {
            sendResponse(socket,"500\nErro interno do gateway");
        }
    }

    private void processMessage(DatagramSocket socket, String[] partes, String servico) {
        if(!validateService(socket, partes, servico)){
            return;
        }

        String operacao = partes[1].trim();
        if(handleHealth(operacao, servico)){
            return;
        }
        if(!validateOperation(socket, partes, servico, operacao)){
            return;
        }

        sendResponse(socket,forwardRequest(servico));
    }

    private boolean validateService(DatagramSocket socket, String[] partes, String servico) {
        if(partes.length < 2){
            sendResponse(socket, "Use servico;operacao;dados");
            return false;
        }
        if(!servico.equals("contaCorrente") && !servico.equals("investimento")){
            sendResponse(socket, "Servico deve ser contaCorrente ou investimento");
            return false;
        }
        return true;
    }

    private boolean handleHealth(String operacao, String servico) {
        if (!operacao.contains("health")){
            return false;
        }
        if(servico.contains("investimento")){
            executor.heartBeatReceived(clientIp,"investimento");
        }
        if(servico.contains("contaCorrente")){
            executor.heartBeatReceived(clientIp,"contaCorrente");
        }
        return true;
    }

    private boolean validateOperation(DatagramSocket socket, String[] partes,
                                      String servico, String operacao) {
        String nome = "";
        String cpf;
        if(operacao.equals("criar")){
            if(partes.length != 4){
                sendResponse(socket, "Use " + servico + ";criar;nome;cpf");
                return false;
            }
            nome = partes[2].trim();
            cpf = partes[3].trim();
            if(nome.isEmpty()){
                sendResponse(socket, "Nome obrigatorio");
                return false;
            }
        } else if(operacao.equals("saldo")){
            if(partes.length != 3){
                sendResponse(socket, "Use " + servico + ";saldo;cpf");
                return false;
            }
            cpf = partes[2].trim();
        } else if(operacao.equals("attsaldo")){
            if(partes.length != 4){
                sendResponse(socket, "Use " + servico + ";attsaldo;cpf;valor");
                return false;
            }
            cpf = partes[2].trim();
        } else if(operacao.equals("resgatar")){
            if(partes.length != 4){
                sendResponse(socket, "Use " + servico + ";resgatar;cpf;valor");
                return false;
            }
            cpf = partes[2].trim();
        } else if(operacao.equals("guardar")){
            if(partes.length != 4){
                sendResponse(socket, "Use " + servico + ";guardar;cpf;valor");
                return false;
            }
            cpf = partes[2].trim();
        } else if(operacao.equals("deletar")){
            if(partes.length != 3){
                sendResponse(socket, "Use " + servico + ";deletar;cpf");
                return false;
            }
            cpf = partes[2].trim();
        } else {
            sendResponse(socket, "Operacao nao permitida");
            return false;
        }
        return true;
    }

    private String forwardRequest(String servico) {
        if (servico.equals("contaCorrente")){
            return clientUDP.request(
                    message,
                    executor.choice("contaCorrente"),
                    "contaCorrente"
            );
        }
        if(servico.equals("investimento")){
            return clientUDP.request(
                    message,
                    executor.choice("investimento"),
                    "investimento"
            );
        }
        return "404\nServico nao encontrado";
    }

    public void sendResponse(DatagramSocket socket, String responseString) {
        if(responseString == null){
            responseString = "503\nservidor indisponivel";
        }
        try {
            byte[] responseBytes = responseString.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length,
                    clientIp, port);
            socket.send(responsePacket);

        } catch (IOException ex) {
        }
    }
}
