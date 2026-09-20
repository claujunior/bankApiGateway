package org.claujunior.twoPhaseCommit;

import java.util.UUID;

public class TransactionRef {
    private UUID txnId;
    private long startTimestamp;

    public TransactionRef(long startTimestamp) {
        this.txnId = UUID.randomUUID();
        this.startTimestamp = startTimestamp;
    }
}
