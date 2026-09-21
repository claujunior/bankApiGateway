package org.claujunior.twoPhaseCommit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionMetadata {

    private final TransactionRef txn;
    private final List<String> participatingKeys =
            new ArrayList<>();
    private final Map<String, InetAddress> participatingServers =
            new ConcurrentHashMap<>();

    private TransactionStatus transactionStatus;


    public TransactionMetadata(TransactionRef txn) {
        this.txn = txn;
        this.transactionStatus = TransactionStatus.STARTED;
    }


    public void addKey(String key) {
        participatingKeys.add(key);
    }


    public List<String> getParticipatingKeys() {
        return participatingKeys;
    }
    public void addServer(String key, InetAddress server) {
        participatingServers.put(key, server);
    }
    public Map<String, InetAddress> getParticipatingServers() {
        return participatingServers;
    }
    public TransactionRef getTxn() {
        return txn;
    }
    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }
    public void setTransactionStatus(
            TransactionStatus transactionStatus
    ) {
        this.transactionStatus = transactionStatus;
    }
}
