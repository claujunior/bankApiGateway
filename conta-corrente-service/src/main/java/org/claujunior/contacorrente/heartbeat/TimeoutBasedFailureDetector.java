package org.claujunior.contacorrente.heartbeat;

import org.claujunior.contacorrente.client.ClientUDP;

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
        clientUDP.request("contaCorrente;health",address);

    }
}
