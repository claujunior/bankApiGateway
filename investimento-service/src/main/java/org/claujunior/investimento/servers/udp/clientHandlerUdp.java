package org.claujunior.investimento.servers.udp;



import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class clientHandlerUdp implements Runnable{
    private DatagramSocket socket;
    private final InetAddress clientIp;
    private final int clientPort;
    private String message;
    public clientHandlerUdp(DatagramSocket socket, DatagramPacket packet){
        this.socket = socket;
        this.message = new String(packet.getData());
        this.clientIp=packet.getAddress();
        this.clientPort=packet.getPort();
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
                sendResponse(socket, 400, "Use servico;operacao;dados");
                return;
            }

            if(!servico.equals("contaCorrente") && !servico.equals("investimento")){
                sendResponse(socket, 400, "Servico deve ser contaCorrente ou investimento");
                return;
            }
            String operacao = partes[1].trim();
            String nome = "";
            String cpf;
            if (operacao.equals("health")){
                sendResponse(socket, 200, servico + ";UP");
                return;
            }
            if(operacao.equals("criar")){
                if(partes.length != 4){
                    sendResponse(socket, 400, "Use " + servico + ";criar;nome;cpf");
                    return;
                }
                nome = partes[2].trim();
                cpf = partes[3].trim();
                if(nome.isEmpty()){
                    sendResponse(socket, 400, "Nome obrigatorio");
                    return;
                }
            } else if(operacao.equals("saldo")){
                if(partes.length != 3){
                    sendResponse(socket, 400, "Use " + servico + ";saldo;cpf");
                    return;
                }
                cpf = partes[2].trim();
            } else if(operacao.equals("attsaldo")){
                if(partes.length != 3){
                    sendResponse(socket, 400, "Use " + servico + ";attsaldo;cpf");
                    return;
                }
                cpf = partes[2].trim();
            } else if(operacao.equals("resgatar")){
                if(partes.length != 3){
                    sendResponse(socket, 400, "Use " + servico + ";resgatar;cpf");
                    return;
                }
                cpf = partes[2].trim();
            } else if(operacao.equals("guardar")){
                if(partes.length != 3){
                    sendResponse(socket, 400, "Use " + servico + ";guardar;cpf");
                    return;
                }
                cpf = partes[2].trim();
            } else if(operacao.equals("deletar")){
                if(partes.length != 3){
                    sendResponse(socket, 400, "Use " + servico + ";deletar;cpf");
                    return;
                }
                cpf = partes[2].trim();
            } else {
                sendResponse(socket, 405, "Operacao nao permitida");
                return;
            }

            if(cpf.isEmpty()){
                sendResponse(socket, 400, "CPF obrigatorio");
                return;
            }
            sendResponse(socket, 200, "Mensagem recebida: " + servico + ";" + operacao);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendResponse(DatagramSocket socket, int statusCode, String responseString) {
        try {
            String response = statusCode + "\n" + responseString;
            byte[] responseBytes = response.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length,
                    clientIp, clientPort);
            socket.send(responsePacket);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
