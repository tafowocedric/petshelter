package com.petshelter.db;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;


class DatabaseInitializer {
    private final DatabaseConnection db;

    DatabaseInitializer(DatabaseConnection db) {
        this.db = db;
    }

    void initialize() {
        try (Connection conn = db.getConnection()) {
            createSchema(conn);
            if (isDatabaseEmpty(conn)) {
                System.out.println("[БД] База данных пуста — загрузка начальных данных...");
                seedData(conn);
            } else {
                System.out.println("[БД] База данных уже содержит данные — пропуск загрузки.");
            }
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Database initialization failed", e);
        }
    }

    private void createSchema(Connection conn) throws SQLException, IOException {
        String sql = loadResource("schema.sql");
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("[БД] Схема проверена/создана.");
        }
    }

    private boolean isDatabaseEmpty(Connection conn) throws SQLException {
        String query = "SELECT (SELECT COUNT(*) FROM users) + (SELECT COUNT(*) FROM animals) AS total";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() && rs.getInt("total") == 0;
        }
    }

    private void seedData(Connection conn) throws SQLException, IOException {
        seedUsers(conn);
        String sql = loadResource("seed.sql");
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("[БД] Начальные данные загружены.");
        }
    }

    private void seedUsers(Connection conn) throws SQLException {
        String hashedPassword = BCrypt.withDefaults().hashToString(12, "admin123".toCharArray());
        String sql = "INSERT INTO users (username, password, full_name, email, phone, role) VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT (username) DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Object[][] users = {
                {"admin",    hashedPassword, "System Administrator", "admin@shelter.com", "+1234567890", "ADMIN"},
                {"john_doe", hashedPassword, "John Doe",             "john@example.com",  "+1234567891", "CLIENT"},
            };
            for (Object[] u : users) {
                for (int i = 0; i < u.length; i++) ps.setString(i + 1, (String) u[i]);
                ps.executeUpdate();
            }
        }
    }

    private String loadResource(String name) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(name)) {
            if (in == null) {
                throw new IOException("Resource not found: " + name);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
    }
}