package com.petshelter.db;

import java.sql.Connection;
import java.sql.SQLException;


public final class Database {
    private static volatile boolean started = false;
    private static DatabaseConnection connection;

    // Prevent instantiation
    private Database() {
        throw new AssertionError("Database is a static façade — do not instantiate.");
    }

    public static synchronized void start() {
        if (started) {
            return;
        }
        try {
            connection = DatabaseConnection.getInstance();
            new DatabaseInitializer(connection).initialize();
            started = true;
            System.out.println("[БД] Модуль запущен успешно.");
        } catch (RuntimeException e) {
            throw new DatabaseException("Failed to start the database module", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        ensureStarted();
        return connection.getConnection();
    }

    public static boolean isStarted() {
        return started;
    }

    public static synchronized void shutdown() {
        if (!started) {
            return;
        }
        connection = null;
        started = false;
        System.out.println("[БД] Модуль остановлен.");
    }

    private static void ensureStarted() {
        if (!started) {
            throw new DatabaseException("Database module not started. Call Database.start() first.");
        }
    }
}