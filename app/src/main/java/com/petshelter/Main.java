package com.petshelter;

import com.petshelter.db.Database;
import com.petshelter.db.DatabaseException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" Pet Shelter Management System — starting up...");
        System.out.println("=================================================");

        try {
            Database.start();
            printRecordCounts();

            System.out.println("=================================================");
            System.out.println(" Application started successfully! ✓");
            System.out.println("=================================================");

        } catch (DatabaseException e) {
            System.err.println("[ERROR] Database module failed: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("[ERROR] SQL error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Database.shutdown();
        }
    }

    private static void printRecordCounts() throws SQLException {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                rs.next();
                System.out.println("[App] Users in DB:     " + rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM animals")) {
                rs.next();
                System.out.println("[App] Animals in DB:   " + rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM adoptions")) {
                rs.next();
                System.out.println("[App] Adoptions in DB: " + rs.getInt(1));
            }
        }
    }
}