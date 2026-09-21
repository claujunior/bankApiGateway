package org.claujunior.investimento.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClientUDP {

    public ClientUDP() {
    }
    public void request(String mensagem , InetAddress enderecoServidor) {

        try (DatagramSocket socket = new DatagramSocket()) {


            byte[] dados = mensagem.getBytes();

            DatagramPacket pacote = new DatagramPacket(
                    dados,
                    dados.length,
                    enderecoServidor,
                    9090
            );

            socket.send(pacote);



        } catch (Exception e) {
            System.err.println("Falha ao enviar heartbeat: " + e.getMessage());
        }
    }
}

