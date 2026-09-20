package org.claujunior.twoPhaseCommit;

import org.claujunior.client.HttpClient;
import org.claujunior.heartbeat.TimeoutBasedFailureDetector;

import java.net.InetAddress;

public class ReplicaMapper {
    private HttpClient httpClient = HttpClient.getInstance();
    private TimeoutBasedFailureDetector<InetAddress> executor = TimeoutBasedFailureDetector.getInstance();

    public InetAddress serverFor(String key){
        String[] selecao = key.split(":");
        String response = httpClient.request(key,"GET","HTTP/1.1","",selecao[0]);
        if(response.contains("true")){
            return executor.choice(selecao[0]);
        }
        return null;
    }

}
