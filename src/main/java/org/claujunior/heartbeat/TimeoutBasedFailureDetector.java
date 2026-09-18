package org.claujunior.heartbeat;

import org.claujunior.client.HttpClient;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TimeoutBasedFailureDetector <T> extends AbstractFailureDetector<T>{
    private static final TimeoutBasedFailureDetector <String> instance = new TimeoutBasedFailureDetector <String>(10000000000l);



    public static TimeoutBasedFailureDetector <String> getInstance() {
        return instance;
    }

    private final long timeoutNanos;

    private TimeoutBasedFailureDetector(long timeoutNanos) {
        this.timeoutNanos = timeoutNanos;
    }

    private final List<T> heartbeatReceivedTimesInvestimento =
            new LinkedList<>();
    private final List<T> heartbeatReceivedTimesContaCorrente =
            new LinkedList<>();
    private final Map<T, Long> heartbeatReceivedTimes =
            new ConcurrentHashMap<>();

    @Override
    void heartBeatCheck() {
        Long now = System.nanoTime();
        Set<T> serverIds = heartbeatReceivedTimes.keySet();
        for (T serverId : serverIds) {
            Long lastHeartbeatReceivedTime = heartbeatReceivedTimes.get(serverId);
            Long timeSinceLastHeartbeat = now - lastHeartbeatReceivedTime;
            if (timeSinceLastHeartbeat >= timeoutNanos) {
                heartbeatReceivedTimes.remove(serverId);
                    if(!heartbeatReceivedTimesContaCorrente.contains(serverId)){
                        heartbeatReceivedTimesContaCorrente.remove(serverId);
                    }
                    else if(!heartbeatReceivedTimesInvestimento.contains(serverId)){
                        heartbeatReceivedTimesInvestimento.remove(serverId);
                    }
                markDown(serverId);
            }
        }
    }

    @Override
    public void heartBeatReceived(T serverId,String servico) {
        Long currentTime = System.nanoTime();
        if(servico.equals("investimento")){
            heartbeatReceivedTimesInvestimento.add(serverId);
        }
        else if(servico.equals("contaCorrente")){
            heartbeatReceivedTimesContaCorrente.add(serverId);
        }
        heartbeatReceivedTimes.put(serverId, currentTime);
        markUp(serverId);
    }
    private void markDown(T serverId) {
        System.out.println(
                "Servidor " + serverId + " caiu"
        );
    }
    private void markUp(T serverId) {
        System.out.println(
                "Servidor " + serverId + " em pe"
        );
    }
    public T choice (String selecao){
        if(selecao=="investimento"){
            if(heartbeatReceivedTimesInvestimento.isEmpty()){
                //ex
            }
            else{
                T resultado = heartbeatReceivedTimesInvestimento.getFirst();
                heartbeatReceivedTimesInvestimento.add(heartbeatReceivedTimesInvestimento.removeFirst());
                return resultado;
            }
        }
        else if(selecao=="contaCorrente"){
            if(heartbeatReceivedTimesContaCorrente.isEmpty()){
                //ex
            }
            else{
                T resultado = heartbeatReceivedTimesContaCorrente.getFirst();
                heartbeatReceivedTimesContaCorrente.add(heartbeatReceivedTimesContaCorrente.removeFirst());
                return resultado;
            }
        }
        else{
            //ex
        }
        return null;
    }
}
