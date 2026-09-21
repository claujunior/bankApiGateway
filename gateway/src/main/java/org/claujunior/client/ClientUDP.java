package org.claujunior.client;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class ClientUDP {

    public ClientUDP() {
    }

    public String request(String mensagem, InetAddress enderecoServidor, String servico) {

        if(enderecoServidor == null){
            return null;
        }

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(10000);
            byte[] dados = mensagem.getBytes();

            int porta = servico.equals("investimento")
                    ? Integer.getInteger("investimento.udp.port", 9092)
                    : Integer.getInteger("contacorrente.udp.port", 9091);

            DatagramPacket pacote = new DatagramPacket(
                    dados,
                    dados.length,
                    enderecoServidor,
                    porta
            );

            socket.send(pacote);

            byte[] resposta = new byte[2048];
            DatagramPacket pacoteResposta =
                    new DatagramPacket(resposta, resposta.length);
            socket.receive(pacoteResposta);
            return new String(
                    pacoteResposta.getData(),
                    pacoteResposta.getOffset(),
                    pacoteResposta.getLength()

            );
        } catch (SocketTimeoutException e) {
            return "504\nTempo esgotado ao acessar " + servico;
        } catch (Exception e) {
            return "502\nFalha ao acessar " + servico;
        }
    }
}
