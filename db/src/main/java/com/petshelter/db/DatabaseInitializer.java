package com.petshelter.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
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
                System.out.println("[DB] Database is empty — seeding initial data...");
                seedData(conn);
            } else {
                System.out.println("[DB] Database already contains data — skipping seed.");
            }
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Database initialization failed", e);
        }
    }

    private void createSchema(Connection conn) throws SQLException, IOException {
        String sql = loadResource("schema.sql");
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("[DB] Schema verified/created.");
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
        String sql = loadResource("seed.sql");
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("[DB] Seed data inserted.");
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