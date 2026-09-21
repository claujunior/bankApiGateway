package org.claujunior.contacorrente.twoPhaseCommit;

import java.util.UUID;
import java.util.Objects;

public class TransactionRef {
    private UUID txnId;
    private long startTimestamp;

    public TransactionRef(long startTimestamp) {
        this.txnId = UUID.randomUUID();
        this.startTimestamp = startTimestamp;
    }

    public TransactionRef(String txnId) {
        this.txnId = UUID.fromString(txnId);
        this.startTimestamp = System.nanoTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionRef that = (TransactionRef) o;
        return Objects.equals(txnId, that.txnId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(txnId);
    }
}

