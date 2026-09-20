package org.claujunior.investimento.heartbeat;

import org.claujunior.investimento.client.ClientUDP;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class TimeoutBasedFailureDetector<T> extends AbstractFailureDetector<T>{
    ClientUDP clientUDP = new ClientUDP();
    InetAddress address = InetAddress.getByName("127.0.0.1");
    public TimeoutBasedFailureDetector() throws UnknownHostException {
    }
    @Override
    void heartBeatSend() {
        clientUDP.request("investimento;health",address);

    }
}


