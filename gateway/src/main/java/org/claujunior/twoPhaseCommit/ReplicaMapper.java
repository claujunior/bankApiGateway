package org.claujunior.twoPhaseCommit;

import org.claujunior.client.HttpClient;
import org.claujunior.heartbeat.TimeoutBasedFailureDetector;

import java.net.InetAddress;

public class ReplicaMapper {
    private HttpClient httpClient = HttpClient.getInstance();
    private TimeoutBasedFailureDetector<InetAddress> executor = TimeoutBasedFailureDetector.getInstance();

    public InetAddress serverFor(String key){
        String[] selecao = key.split(":");
        String servico = selecao[0];
        String cpf = selecao[1];
        String recurso = "/" + servico + "/saldo/" + cpf + "/transacao";
        InetAddress server = executor.choice(servico);
        String response = httpClient.request(recurso,"GET","HTTP/1.1","",servico,server);
        if(response.contains("true")){
            return server;
        }
        return null;
    }

}
