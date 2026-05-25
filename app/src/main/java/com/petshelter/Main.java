package com.petshelter;

import com.petshelter.db.Database;
import com.petshelter.db.DatabaseException;
import com.petshelter.enums.*;
import com.petshelter.exception.*;
import com.petshelter.model.*;
import com.petshelter.repository.AdoptionRepository;
import com.petshelter.repository.AnimalRepository;
import com.petshelter.repository.JoinedAdoption;
import com.petshelter.repository.UserRepository;
import com.petshelter.util.PasswordHasher;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

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
            demoUserRepository();
            demoAnimalRepository();
            demoAdoptionRepository();
            demoPasswordHasher();

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
        User admin = new Admin("admin", PasswordHasher.hash("hash"), "System Administrator", "admin@shelter.com", "+1234567890");
        User client = new Client("john_doe", PasswordHasher.hash("hash"), "John Doe", "john@example.com", "+1234567891");

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

    private static void demoUserRepository() {
        System.out.println("\n--- UserRepository Demo ---");

        UserRepository userRepo = new UserRepository();

        try {
            // Count and list existing users (from seed data)
            System.out.println("Total users: " + userRepo.count());

            for (User u : userRepo.findAll()) {
                System.out.println("  " + u);
            }

            // findByUsername — uses the seed admin
            Optional<User> admin = userRepo.findByUsername("admin");
            System.out.println("\nLookup 'admin': " +
                    admin.map(u -> u + " [class=" + u.getClass().getSimpleName() + "]")
                            .orElse("(not found)"));

            // findByEmail
            Optional<User> john = userRepo.findByEmail("john@example.com");
            System.out.println("Lookup 'john@example.com': " +
                    john.map(User::toString).orElse("(not found)"));

            // findByRole
            System.out.println("\nAll admins:");
            for (User u : userRepo.findByRole(UserRole.ADMIN)) {
                System.out.println("  " + u);
            }

            // CRUD round-trip on a temporary user
            Client temp = new Client("temp_user_" + System.currentTimeMillis(),
                    "fakehash", "Temporary Tester", "temp" + System.currentTimeMillis() + "@example.com", "+10000000000");

            User saved = userRepo.save(temp);
            System.out.println("\nSaved new user with id=" + saved.getId());

            saved.setFullName("Updated Tester");
            userRepo.update(saved);
            System.out.println("Updated full name -> " + userRepo.findById(saved.getId()).get().getFullName());

            boolean deleted = userRepo.deleteById(saved.getId());
            System.out.println("Deleted temp user: " + deleted);
            System.out.println("Total users after cleanup: " + userRepo.count());

            // Try a duplicate to see the custom exception
            try {
                Admin dup = new Admin("admin", "hash", "Dup Admin", "dup@example.com", "+1");
                userRepo.save(dup);
            } catch (DuplicateUserException e) {
                System.out.println("Caught expected: " + e.getMessage());
            }

        } catch (ShelterException e) {
            System.err.println("Repository error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void demoAnimalRepository() {
        System.out.println("\n--- AnimalRepository Demo ---");

        AnimalRepository animalRepo = new AnimalRepository();

        try {
            // count
            System.out.println("Total animals: " + animalRepo.count());
            System.out.println("Available:     " + animalRepo.countByStatus(AnimalStatus.AVAILABLE));

            // findAll — polymorphic mapping
            System.out.println("\nAll animals (polymorphic mapping):");
            for (Animal a : animalRepo.findAll()) {
                System.out.println("  " + a.getInfo()
                        + "  [class=" + a.getClass().getSimpleName() + "]"
                        + "  sound=" + a.makeSound());
            }

            // Explicit finder methods
            System.out.println("\nFinder demos:");

            Optional<Animal> byId   = animalRepo.findById(1);
            Optional<Animal> byName = animalRepo.findByName("Whiskers");
            List<Animal> dogs       = animalRepo.findBySpecies(Species.DOG);
            List<Animal> available  = animalRepo.findByStatus(AnimalStatus.AVAILABLE);
            List<Animal> availDogs  = animalRepo.findBySpeciesAndStatus(Species.DOG, AnimalStatus.AVAILABLE);

            System.out.println("  findById(1):                       " + byId.map(Animal::getName).orElse("(none)"));
            System.out.println("  findByName(\"Whiskers\"):            " + byName.map(Animal::getName).orElse("(none)"));
            System.out.println("  findBySpecies(DOG):                " + dogs.size() + " dog(s)");
            System.out.println("  findByStatus(AVAILABLE):           " + available.size() + " animal(s)");
            System.out.println("  findBySpeciesAndStatus(DOG, AVAIL): " + availDogs.size() + " available dog(s)");

            // CRUD round-trip
            Cat newCat = new Cat("TestKitty", "Tabby", 1, Gender.FEMALE,
                    new BigDecimal("3.50"), "Black", "A temporary test cat", true);
            Animal saved = animalRepo.save(newCat);
            System.out.println("\nSaved new cat with id=" + saved.getId());

            saved.setDescription("Updated description");
            animalRepo.update(saved);

            Optional<Animal> reloaded = animalRepo.findById(saved.getId());
            System.out.println("Reloaded: " + reloaded.map(Animal::getDescription).orElse("(missing)"));

            boolean deleted = animalRepo.deleteById(saved.getId());
            System.out.println("Deleted: " + deleted);
            System.out.println("Total after cleanup: " + animalRepo.count());

        } catch (ShelterException e) {
            System.err.println("Repository error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void demoAdoptionRepository() {
        System.out.println("\n--- AdoptionRepository Demo ---");

        AnimalRepository animalRepo = new AnimalRepository();
        UserRepository userRepo = new UserRepository();
        AdoptionRepository adoptionRepo = new AdoptionRepository();

        try {
            // Find a client and an available animal from seed data
            User client = userRepo.findByUsername("john_doe").orElseThrow();
            Animal animal = animalRepo.findAvailable().get(0);

            System.out.println("Using client: " + client.getDisplayName(true));
            System.out.println("Using animal: " + animal.getInfo());

            // Create a new adoption
            Adoption adoption = new Adoption(animal.getId(), client.getId());
            adoption.setNotes("Demo adoption from Main");
            Adoption saved = adoptionRepo.save(adoption);
            System.out.println("\nSaved adoption with id=" + saved.getId() + ", status=" + saved.getStatus());

            // Show count
            System.out.println("Total adoptions in DB: " + adoptionRepo.count());

            // findJoinedById — single join query returns adoption + animal + client
            Optional<JoinedAdoption> ja = adoptionRepo.findJoinedById(saved.getId());
            ja.ifPresent(j -> {
                System.out.println("\nJoined fetch (single round-trip):");
                System.out.println("  " + j);
                System.out.println("  Animal class: " + j.getAnimal().getClass().getSimpleName());
                System.out.println("  Client class: " + j.getClient().getClass().getSimpleName());
            });

            // Filter — by status
            System.out.println("\nPending adoptions:");
            for (JoinedAdoption j : adoptionRepo.findJoinedByStatus(AdoptionStatus.PENDING)) {
                System.out.println("  " + j);
            }

            // Filter — by client
            System.out.println("\nAdoptions for client " + client.getDisplayName() + ":");
            for (JoinedAdoption j : adoptionRepo.findJoinedByClient(client.getId())) {
                System.out.println("  " + j);
            }

            // hasActiveAdoption — business check
            System.out.println("\nhasActiveAdoption(" + animal.getId() + ") = "
                    + adoptionRepo.hasActiveAdoption(animal.getId()));

            // Update the status (approval flow preview)
            User admin = userRepo.findByUsername("admin").orElseThrow();
            saved.setStatus(AdoptionStatus.APPROVED);
            saved.setApprovedBy(admin.getId());
            adoptionRepo.update(saved);
            System.out.println("\nAfter approval, status = "
                    + adoptionRepo.findById(saved.getId()).get().getStatus());

            // Build a receipt using the nested class from Phase 2
            ja = adoptionRepo.findJoinedById(saved.getId());
            if (ja.isPresent()) {
                Adoption.Receipt receipt = ja.get().getAdoption()
                        .buildReceipt(ja.get().getAnimal(), ja.get().getClient());
                System.out.println("\nReceipt for approved adoption:");
                System.out.println(receipt.format());
            }

            // Cleanup
            boolean deleted = adoptionRepo.deleteById(saved.getId());
            System.out.println("Deleted demo adoption: " + deleted);
            System.out.println("Total adoptions after cleanup: " + adoptionRepo.count());

        } catch (ShelterException e) {
            System.err.println("Repository error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void demoPasswordHasher() {
        System.out.println("\n--- PasswordHasher Demo (BCrypt) ---");

        // Hash the same password twice — each hash is different (random salt)
        String h1 = PasswordHasher.hash("admin123");
        String h2 = PasswordHasher.hash("admin123");
        System.out.println("hash #1: " + h1);
        System.out.println("hash #2: " + h2);
        System.out.println("different hashes (salt works): " + !h1.equals(h2));

        // Both still verify against the original password
        System.out.println("verify(\"admin123\", h1) = " + PasswordHasher.verify("admin123", h1));
        System.out.println("verify(\"admin123\", h2) = " + PasswordHasher.verify("admin123", h2));
        System.out.println("verify(\"wrong\",    h1) = " + PasswordHasher.verify("wrong",    h1));

        // Sanity-check that the seed hashes verify against the known plain-text
        String seedAdmin = "$2a$12$3euPcmQFCiblsZeEu5s7p.9MQICjYJ7DjRHGqlObPMtAOTd0sCqDC";
        String seedJohn  = "$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        System.out.println("seed admin verifies: " + PasswordHasher.verify("admin123", seedAdmin));
        System.out.println("seed john  verifies: " + PasswordHasher.verify("pass123",  seedJohn));
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