package org.claujunior.contacorrente.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private final String url;

    public Database(Path file) {
        Path absolute = file.toAbsolutePath().normalize();
        try {
            Files.createDirectories(absolute.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel criar a pasta do banco", e);
        }
        url = "jdbc:sqlite:" + absolute;
        try (var connection = open(); var statement = connection.createStatement()) {
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS contas (
                    cpf TEXT PRIMARY KEY NOT NULL,
                    nome TEXT NOT NULL,
                    saldo_centavos INTEGER NOT NULL DEFAULT 0
                        CHECK (saldo_centavos BETWEEN 0 AND 99999999999999)
                )
                """);
        } catch (SQLException e) {
            throw new IllegalStateException("Nao foi possivel inicializar o banco", e);
        }
    }

    public static Database configured() {
        String file = System.getProperty("contacorrente.db.path");
        if (file == null || file.isBlank()) file = System.getenv("CONTA_CORRENTE_DB_PATH");
        if (file == null || file.isBlank()) file = "data/conta-corrente.db";
        return new Database(Path.of(file));
    }

    public Connection open() throws SQLException {
        var properties = new java.util.Properties();
        properties.setProperty("busy_timeout", "10000");
        return DriverManager.getConnection(url, properties);
    }
}
