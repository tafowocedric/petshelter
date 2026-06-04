package com.petshelter.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


class DatabaseConnection {
    private static DatabaseConnection instance;
    private final String url;

    private DatabaseConnection() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new DatabaseException("config.properties not found on classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new DatabaseException("Failed to load config.properties", e);
        }

        this.url = props.getProperty("db.url");
    }

    static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        try (var stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }
}
