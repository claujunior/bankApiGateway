package org.claujunior.contacorrente.heartbeat;

import org.claujunior.contacorrente.client.ClientUDP;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class TimeoutBasedFailureDetector<T> extends AbstractFailureDetector<T>{
    ClientUDP clientUDP = new ClientUDP();
    InetAddress address = InetAddress.getByName("127.0.0.1");
    public TimeoutBasedFailureDetector() throws UnknownHostException {
    }
    @Override
    void heartBeatSend() {
        clientUDP.request("contaCorrente;health",address);

    }
}
