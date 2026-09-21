package org.claujunior.contacorrente.twoPhaseCommit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionState {

    private final Map<String, String> pendingUpdates =
            new ConcurrentHashMap<>();
    private TransactionStatus status = TransactionStatus.STARTED;

    public void addPendingUpdate(String key, String value) {
        pendingUpdates.put(key, value);
    }

    public Map<String, String> getPendingUpdates() {
        return pendingUpdates;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}
