package org.claujunior.investimento.heartbeat;

import org.claujunior.investimento.client.ClientUDP;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class TimeoutBasedFailureDetector<T> extends AbstractFailureDetector<T>{
    ClientUDP clientUDP = new ClientUDP();
    InetAddress address;
    public TimeoutBasedFailureDetector() throws UnknownHostException {
        String gatewayHost = System.getProperty("gateway.host");
        if (gatewayHost == null || gatewayHost.isBlank()) {
            gatewayHost = System.getenv("GATEWAY_HOST");
        }
        if (gatewayHost == null || gatewayHost.isBlank()) {
            gatewayHost = "127.0.0.1";
        }
        address = InetAddress.getByName(gatewayHost);
    }
    @Override
    void heartBeatSend() {
        clientUDP.request("investimento;health",address);

    }
}

