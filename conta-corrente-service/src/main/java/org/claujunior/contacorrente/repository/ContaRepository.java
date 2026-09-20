package org.claujunior.contacorrente.repository;

import org.claujunior.contacorrente.database.Database;
import org.claujunior.contacorrente.service.ServiceException;
import org.claujunior.contacorrente.model.ContaResumo;
import java.math.BigDecimal;
import java.sql.SQLException;

public final class ContaRepository {
    private final Database database;

    public ContaRepository(Database database) { this.database = database; }

    public void criar(String nome, String cpf, long saldo) throws SQLException {
        try (var connection = database.open();
             var statement = connection.prepareStatement(
                 "INSERT INTO contas (cpf, nome, saldo_centavos) VALUES (?, ?, ?) ON CONFLICT(cpf) DO NOTHING")) {
            statement.setString(1, cpf);
            statement.setString(2, nome);
            statement.setLong(3, saldo);
            if (statement.executeUpdate() == 0)
                throw new ServiceException(409, "CPF ja cadastrado");
        }
    }

    public ContaResumo buscarPorCpf(String cpf) throws SQLException {
        try (var connection = database.open();
             var statement = connection.prepareStatement("SELECT nome, saldo_centavos FROM contas WHERE cpf = ?")) {
            statement.setString(1, cpf);
            try (var result = statement.executeQuery()) {
                if (!result.next()) throw new ServiceException(404, "Conta nao encontrada");
                return new ContaResumo(result.getString("nome"),
                        BigDecimal.valueOf(result.getLong("saldo_centavos"), 2));
            }
        }
    }

    public long saldo(String cpf) throws SQLException {
        try (var connection = database.open();
             var statement = connection.prepareStatement("SELECT saldo_centavos FROM contas WHERE cpf = ?")) {
            statement.setString(1, cpf);
            try (var result = statement.executeQuery()) {
                if (!result.next()) throw new ServiceException(404, "Conta nao encontrada");
                return result.getLong(1);
            }
        }
    }

    // O incremento e a verificacao de saldo ocorrem na mesma instrucao SQL.
    // Assim, requisicoes concorrentes nao sobrescrevem o saldo umas das outras.
    public long movimentar(String cpf, long valor) throws SQLException {
        try (var connection = database.open();
             var statement = connection.prepareStatement("""
                 UPDATE contas SET saldo_centavos = saldo_centavos + ?
                 WHERE cpf = ? AND saldo_centavos + ? BETWEEN 0 AND 99999999999999
                 RETURNING saldo_centavos
                 """)) {
            statement.setLong(1, valor);
            statement.setString(2, cpf);
            statement.setLong(3, valor);
            try (var result = statement.executeQuery()) {
                if (result.next()) return result.getLong(1);
            }
        }
        saldo(cpf); // Distingue conta inexistente de saldo insuficiente.
        throw new ServiceException(409, "Saldo insuficiente ou limite de saldo excedido");
    }

    public void deletar(String cpf) throws SQLException {
        try (var connection = database.open();
             var statement = connection.prepareStatement("DELETE FROM contas WHERE cpf = ? AND saldo_centavos = 0")) {
            statement.setString(1, cpf);
            if (statement.executeUpdate() == 1) return;
        }
        saldo(cpf);
        throw new ServiceException(409, "A conta deve ter saldo zero para ser removida");
    }

    public void verificarConexao() throws SQLException {
        try (var connection = database.open(); var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT 1")) {
            if (!result.next()) throw new SQLException("Banco indisponivel");
        }
    }
}
