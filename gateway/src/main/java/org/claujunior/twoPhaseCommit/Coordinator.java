package org.claujunior.twoPhaseCommit;

import org.claujunior.client.HttpClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Coordinator {
    private static final Coordinator instance = new Coordinator();

    private Coordinator(){};

    public static Coordinator getInstance() {
        return instance;
    }
    private final Map<TransactionRef, TransactionMetadata> transactions =
            new ConcurrentHashMap<>();

    public void begin(TransactionRef transactionRef) {

        TransactionMetadata metadata =
                new TransactionMetadata(transactionRef);

        transactions.put(
                transactionRef,
                metadata
        );
    }

    public void addKeyToTransaction(
            TransactionRef transactionRef,
            String key
    ) {

        TransactionMetadata metadata =
                transactions.get(transactionRef);

        if (metadata == null) {
            throw new IllegalArgumentException(
                    "Transação não encontrada"
            );
        }

        if (!metadata
                .getParticipatingKeys()
                .contains(key)) {

            metadata.addKey(key);
        }
    }

}
