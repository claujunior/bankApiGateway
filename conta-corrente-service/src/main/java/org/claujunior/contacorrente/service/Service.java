package org.claujunior.contacorrente.service;

import org.claujunior.contacorrente.database.Database;
import org.claujunior.contacorrente.repository.ContaRepository;
import org.claujunior.contacorrente.model.ContaResumo;
import java.math.BigDecimal;
import java.sql.SQLException;

public final class Service {
    private static final long LIMITE = 99999999999999L;
    private final ContaRepository repository;
    private static final Service instance = new Service();

    private Service() {
        repository = new ContaRepository(Database.configured());
    }
    public static Service getInstance() { return instance; }

    public void criar(String nome, String cpf, BigDecimal saldoInicial) throws SQLException {
        if (nome == null || nome.isBlank() || nome.trim().length() > 200)
            throw new ServiceException(400, "Nome obrigatorio, com ate 200 caracteres");
        long saldo = centavos(saldoInicial);
        if (saldo < 0) throw new ServiceException(400, "Saldo inicial nao pode ser negativo");
        repository.criar(nome.trim(), validarCpf(cpf), saldo);
    }

    public ContaResumo consultarConta(String cpf) throws SQLException {
        return repository.buscarPorCpf(validarCpf(cpf));
    }

    public BigDecimal saldo(String cpf) throws SQLException {
        return BigDecimal.valueOf(repository.saldo(validarCpf(cpf)), 2);
    }

    public BigDecimal atualizarSaldo(String cpf, BigDecimal valor) throws SQLException {
        long delta = centavos(valor);
        if (delta == 0) throw new ServiceException(400, "Valor deve ser diferente de zero");
        return BigDecimal.valueOf(repository.movimentar(validarCpf(cpf), delta), 2);
    }

    public BigDecimal guardar(String cpf, BigDecimal valor) throws SQLException {
        positivo(valor);
        return atualizarSaldo(cpf, valor);
    }

    public BigDecimal resgatar(String cpf, BigDecimal valor) throws SQLException {
        positivo(valor);
        return atualizarSaldo(cpf, valor.negate());
    }

    public void deletar(String cpf) throws SQLException { repository.deletar(validarCpf(cpf)); }

    public String executar(String operacao, String... dados) throws SQLException {

        if (operacao.contains("criar")) {
            quantidade(dados, 2);
            criar(dados[0], dados[1], BigDecimal.ZERO);
            return "Conta criada";
        } else if (operacao.contains("att")) {
            quantidade(dados, 2);
            return atualizarSaldo(dados[0], valor(dados[1])).toPlainString();
        } else if (operacao.contains("saldo")) {
            quantidade(dados, 1);
            return saldo(dados[0]).toPlainString();
        } else if (operacao.contains("deletar")) {
            quantidade(dados, 1);
            deletar(dados[0]);
            return "Conta removida";
        } else if (operacao.contains("guardar")) {
            quantidade(dados, 2);
            return guardar(dados[0], valor(dados[1])).toPlainString();
        } else if (operacao.contains("resgatar")) {
            quantidade(dados, 2);
            return resgatar(dados[0], valor(dados[1])).toPlainString();
        }

        throw new ServiceException(405, "Operacao nao permitida neste servico");
    }

    private static void quantidade(String[] dados, int esperada) {
        if (dados.length != esperada)
            throw new ServiceException(400, "Quantidade de argumentos invalida; consulte o README");
    }

    private static BigDecimal valor(String texto) {
        try { return new BigDecimal(texto); }
        catch (NumberFormatException e) { throw new ServiceException(400, "Valor monetario invalido"); }
    }

    private static void positivo(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0)
            throw new ServiceException(400, "Valor deve ser positivo");
    }

    private static long centavos(BigDecimal valor) {
        if (valor == null) throw new ServiceException(400, "Valor obrigatorio");
        try {
            long cents = valor.movePointRight(2).longValueExact();
            if (cents < -LIMITE || cents > LIMITE) throw new ArithmeticException();
            return cents;
        } catch (ArithmeticException e) {
            throw new ServiceException(400, "Valor fora do limite ou com mais de duas casas decimais");
        }
    }

    private static String validarCpf(String cpf) {
        if (cpf == null) throw new ServiceException(400, "CPF obrigatorio");
        String normalizado = cpf.trim().replace(".", "").replace("-", "");
        if (!normalizado.matches("[0-9]{11}"))
            throw new ServiceException(400, "CPF deve conter 11 digitos");
        return normalizado;
    }
}
