package org.claujunior.investimento.twoPhaseCommit;

import org.claujunior.investimento.service.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionalKVStore {
    private static final BigDecimal SALDO_MAXIMO = new BigDecimal("999999999999.99");
    private static TransactionalKVStore instance = new TransactionalKVStore();
    private TransactionalKVStore(){};
    private Service service = Service.getInstance();
    public static TransactionalKVStore getInstance(){
        return instance;
    }
    private final Map<TransactionRef, TransactionState> ongoingTransactions =
            new ConcurrentHashMap<>();
    private final Map<TransactionRef, TransactionStatus> completedTransactions =
            new ConcurrentHashMap<>();

    private TransactionState getOrCreateTransactionState(
            TransactionRef transactionRef
    ) {
        return ongoingTransactions.computeIfAbsent(
                transactionRef,
                key -> new TransactionState()
        );
    }
    public synchronized boolean put(
            TransactionRef transactionRef,
            String key,
            String value
    ) {
        if (completedTransactions.containsKey(transactionRef)) {
            return false;
        }

        TransactionState state =
                getOrCreateTransactionState(transactionRef);

        if (state.getStatus() != TransactionStatus.STARTED) {
            return false;
        }

        state.addPendingUpdate(key, value);
        return true;
    }

    public synchronized boolean prepare(TransactionRef transactionRef) {
        TransactionStatus completedStatus =
                completedTransactions.get(transactionRef);

        if (completedStatus == TransactionStatus.COMMITTED) {
            return true;
        }
        if (completedStatus == TransactionStatus.ABORTED) {
            return false;
        }

        TransactionState state =
                ongoingTransactions.get(transactionRef);

        if (state == null) {
            return false;
        }
        if (state.getStatus() == TransactionStatus.PREPARED) {
            return true;
        }

        try {
            for (Map.Entry<String, String> update :
                    state.getPendingUpdates().entrySet()) {

                String[] partes = update.getKey().split(":", 2);

                if (partes.length != 2) {
                    return false;
                }
                if (!partes[0].equals("investimento")) {
                    return false;
                }

                String cpf = partes[1];
                BigDecimal valor =
                        new BigDecimal(update.getValue());

                if (valor.signum() == 0) {
                    return false;
                }

                BigDecimal saldoAtual = service.saldo(cpf);
                BigDecimal saldoDepoisDaTransacao = saldoAtual.add(valor);

                if (saldoDepoisDaTransacao.signum() < 0 ||
                        saldoDepoisDaTransacao.compareTo(SALDO_MAXIMO) > 0) {
                    return false;
                }
            }

            state.setStatus(TransactionStatus.PREPARED);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public synchronized boolean commit(TransactionRef transactionRef) {
        TransactionStatus completedStatus =
                completedTransactions.get(transactionRef);

        if (completedStatus == TransactionStatus.COMMITTED) {
            return true;
        }
        if (completedStatus == TransactionStatus.ABORTED) {
            return false;
        }

        TransactionState state =
                ongoingTransactions.get(transactionRef);

        if (state == null) {
            return false;
        }

        if (state.getStatus() != TransactionStatus.PREPARED) {
            return false;
        }

        try {
            for (Map.Entry<String, String> update :
                    state.getPendingUpdates().entrySet()) {

                String[] partes = update.getKey().split(":", 2);

                if (partes.length != 2) {
                    return false;
                }
                if (!partes[0].equals("investimento")) {
                    return false;
                }

                String cpf = partes[1];
                BigDecimal valor =
                        new BigDecimal(update.getValue());

                service.atualizarSaldo(cpf, valor);
            }

            state.setStatus(TransactionStatus.COMMITTED);
            completedTransactions.put(
                    transactionRef,
                    TransactionStatus.COMMITTED
            );
            ongoingTransactions.remove(transactionRef);

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public synchronized boolean abort(TransactionRef transactionRef) {
        TransactionStatus completedStatus =
                completedTransactions.get(transactionRef);

        if (completedStatus == TransactionStatus.COMMITTED) {
            return false;
        }
        if (completedStatus == TransactionStatus.ABORTED) {
            return true;
        }

        TransactionState state = ongoingTransactions.remove(transactionRef);

        if (state == null) {
            return false;
        }

        state.setStatus(TransactionStatus.ABORTED);
        completedTransactions.put(
                transactionRef,
                TransactionStatus.ABORTED
        );
        return true;
    }
}
