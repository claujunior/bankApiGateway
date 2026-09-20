package org.claujunior.twoPhaseCommit;

import java.util.ArrayList;
import java.util.List;

public class TransactionMetadata {

    private final TransactionRef txn;
    private final List<String> participatingKeys =
            new ArrayList<>();

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
