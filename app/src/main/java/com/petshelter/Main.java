package com.petshelter;

import com.petshelter.db.Database;
import com.petshelter.db.DatabaseException;
import com.petshelter.enums.Gender;
import com.petshelter.exception.*;
import com.petshelter.model.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" Pet Shelter Management System — starting up...");
        System.out.println("=================================================");

        try {
            Database.start();
            printRecordCounts();
            demoDomainModel();
            demoExceptions();

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

    private static void demoDomainModel() {
        System.out.println("\n--- Domain Model Demo ---");

        // Build a few animals — note we use the base type Animal
        List<Animal> animals = List.of(
            new Dog("Rex", "Labrador", 3, Gender.MALE, new BigDecimal("25.5"), "Golden", "Friendly", true),
            new Cat("Whiskers", "Persian", 2, Gender.FEMALE, new BigDecimal("4.2"), "White", "Calm", true),
            new Bird("Tweety", "Canary", 1, Gender.MALE, new BigDecimal("0.02"), "Yellow", "Sings", true)
        );

        // [POLYMORPHISM] — same call, different behavior per subclass
        for (Animal a : animals) {
            System.out.println(a.getInfo());
            System.out.println("  Sound: " + a.makeSound());
            System.out.println("  Care:  " + a.getCareInstructions());
        }

        // Users
        User admin = new Admin("admin", "hash", "System Administrator", "admin@shelter.com", "+1234567890");
        User client = new Client("john_doe", "hash", "John Doe", "john@example.com", "+1234567891");

        // [METHOD OVERLOADING]
        System.out.println("\nDisplay name plain:    " + admin.getDisplayName());
        System.out.println("Display name + user:   " + admin.getDisplayName(true));
        System.out.println("Display name prefixed: " + client.getDisplayName("Mr."));

        // Adoption + [NESTED CLASS] receipt
        Adoption adoption = new Adoption(1, 2);
        Adoption.Receipt receipt = adoption.buildReceipt(animals.get(0), client);
        System.out.println("\n" + receipt.format());
    }

    private static void demoExceptions() {
        System.out.println("\n--- Exception Hierarchy Demo ---");

        // Catching the specific subclass
        try {
            throw new AnimalNotFoundException(42);
        } catch (AnimalNotFoundException e) {
            System.out.println("Caught specific:   " + e.getMessage());
        } catch (ShelterException e) {
            System.out.println("Caught general:    " + e.getMessage());
        }

        // Catching via the base type — works for ANY ShelterException
        try {
            throw new InvalidCredentialsException();
        } catch (ShelterException e) {
            System.out.println("Caught as base:    " + e.getClass().getSimpleName()
                    + " — " + e.getMessage());
        }

        // Exception with cause (chained)
        try {
            try {
                throw new IllegalStateException("DB down");
            } catch (IllegalStateException root) {
                throw new UserNotFoundException("admin lookup failed", root);
            }
        } catch (UserNotFoundException e) {
            System.out.println("Chained exception: " + e.getMessage()
                    + " (caused by: " + e.getCause().getMessage() + ")");
        }

        // Business-rule exception
        try {
            throw new AnimalNotAvailableException(5, "ADOPTED");
        } catch (ShelterException e) {
            System.out.println("Business rule:     " + e.getMessage());
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