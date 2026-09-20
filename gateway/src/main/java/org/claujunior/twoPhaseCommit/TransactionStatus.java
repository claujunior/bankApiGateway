package org.claujunior.twoPhaseCommit;

public enum TransactionStatus {
    STARTED,
    PREPARING,
    PREPARED,
    COMMITTED,
    ROLLED_BACK
}