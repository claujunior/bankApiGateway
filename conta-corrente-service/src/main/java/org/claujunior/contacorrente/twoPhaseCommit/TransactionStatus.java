package org.claujunior.contacorrente.twoPhaseCommit;

public enum TransactionStatus {
    STARTED,
    PREPARED,
    COMMITTED,
    ABORTED
}
