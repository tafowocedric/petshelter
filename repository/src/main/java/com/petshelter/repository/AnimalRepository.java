package com.petshelter.repository;

import com.petshelter.db.Database;
import com.petshelter.enums.AnimalStatus;
import com.petshelter.enums.Gender;
import com.petshelter.enums.Species;
import com.petshelter.exception.AnimalNotFoundException;
import com.petshelter.exception.ShelterException;
import com.petshelter.model.Animal;
import com.petshelter.model.Bird;
import com.petshelter.model.Cat;
import com.petshelter.model.Dog;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC repository for Animal entities.
 * [INTERFACES] [METHOD OVERLOADING] [POLYMORPHISM] [DATA TYPES]
 */
public class AnimalRepository implements Repository<Animal, Integer> {
    // Columns selected by all read queries (kept consistent)
    private static final String COLS =
        "id, name, species, breed, age, gender, weight, color, description, " +
            "status, arrival_date, is_trained, is_indoor, can_fly";

    // SQL constants
    private static final String SQL_INSERT =
        "INSERT INTO animals (" + COLS.replace("id, ", "") + ") " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "RETURNING id, arrival_date";

    private static final String SQL_UPDATE =
        "UPDATE animals SET name = ?, species = ?, breed = ?, age = ?, gender = ?, " +
            "weight = ?, color = ?, description = ?, status = ?, arrival_date = ?, " +
            "is_trained = ?, is_indoor = ?, can_fly = ? WHERE id = ?";

    private static final String SQL_DELETE = "DELETE FROM animals WHERE id = ?";
    private static final String SQL_FIND_BY_ID = "SELECT " + COLS + " FROM animals WHERE id = ?";
    private static final String SQL_FIND_BY_NAME = "SELECT " + COLS + " FROM animals WHERE LOWER(name) = LOWER(?)";
    private static final String SQL_FIND_BY_SPECIES = "SELECT " + COLS + " FROM animals WHERE species = ? ORDER BY name";
    private static final String SQL_FIND_BY_STATUS = "SELECT " + COLS + " FROM animals WHERE status = ? ORDER BY arrival_date DESC";
    private static final String SQL_FIND_AVAILABLE = "SELECT " + COLS + " FROM animals WHERE status = 'AVAILABLE' ORDER BY arrival_date DESC";
    private static final String SQL_FIND_BY_SPECIES_AND_STATUS = "SELECT " + COLS + " FROM animals WHERE species = ? AND status = ? ORDER BY name";
    private static final String SQL_FIND_ALL = "SELECT " + COLS + " FROM animals ORDER BY id";
    private static final String SQL_EXISTS = "SELECT 1 FROM animals WHERE id = ?";
    private static final String SQL_COUNT = "SELECT COUNT(*) FROM animals";
    private static final String SQL_COUNT_BY_STATUS = "SELECT COUNT(*) FROM animals WHERE status = ?";

    // CREATE
    @Override
    public Animal save(Animal animal) throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {

            bindAnimalFields(ps, animal);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    animal.setId(rs.getInt("id"));
                    animal.setArrivalDate(rs.getDate("arrival_date").toLocalDate());
                }
            }
            return animal;

        } catch (SQLException e) {
            throw new ShelterException("Failed to save animal", e);
        }
    }

    // UPDATE
    @Override
    public Animal update(Animal animal) throws ShelterException {
        if (animal.getId() == null) {
            throw new ShelterException("Cannot update animal without id");
        }
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            bindAnimalFields(ps, animal);
            ps.setInt(14, animal.getId()); // last "?" is the id

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new AnimalNotFoundException(animal.getId());
            }
            return animal;

        } catch (SQLException e) {
            throw new ShelterException("Failed to update animal", e);
        }
    }

    // READ — [METHOD OVERLOADING]
    @Override
    public Optional<Animal> findById(Integer id) throws ShelterException {
        return findBy(SQL_FIND_BY_ID, ps -> ps.setInt(1, id));
    }

    public Optional<Animal> findByName(String name) throws ShelterException {
        return findBy(SQL_FIND_BY_NAME, ps -> ps.setString(1, name));
    }

    public List<Animal> findBySpecies(Species species) throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_SPECIES)) {
            ps.setString(1, species.name());
            return collect(ps);
        } catch (SQLException e) {
            throw new ShelterException("Failed to query animals by species", e);
        }
    }

    public List<Animal> findByStatus(AnimalStatus status) throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_STATUS)) {
            ps.setString(1, status.name());
            return collect(ps);
        } catch (SQLException e) {
            throw new ShelterException("Failed to query animals by status", e);
        }
    }

    public List<Animal> findBySpeciesAndStatus(Species species, AnimalStatus status) throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_SPECIES_AND_STATUS)) {
            ps.setString(1, species.name());
            ps.setString(2, status.name());
            return collect(ps);
        } catch (SQLException e) {
            throw new ShelterException("Failed to query animals by species+status", e);
        }
    }

    /** Convenience: animals currently available for adoption. */
    public List<Animal> findAvailable() throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_AVAILABLE)) {
            return collect(ps);
        } catch (SQLException e) {
            throw new ShelterException("Failed to query available animals", e);
        }
    }

    @Override
    public List<Animal> findAll() throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL)) {
            return collect(ps);
        } catch (SQLException e) {
            throw new ShelterException("Failed to query animals", e);
        }
    }

    // DELETE
    @Override
    public boolean deleteById(Integer id) throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ShelterException("Failed to delete animal " + id, e);
        }
    }

    // UTIL
    @Override
    public boolean existsById(Integer id) throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_EXISTS)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ShelterException("Failed to check animal existence", e);
        }
    }

    @Override
    public long count() throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new ShelterException("Failed to count animals", e);
        }
    }

    public long countByStatus(AnimalStatus status) throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_BY_STATUS)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new ShelterException("Failed to count animals by status", e);
        }
    }

    // PRIVATE HELPERS
    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private Optional<Animal> findBy(String sql, StatementBinder binder) throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ShelterException("Query failed", e);
        }
    }

    private List<Animal> collect(PreparedStatement ps) throws SQLException {
        List<Animal> out = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(mapRow(rs));
            }
        }
        return out;
    }

    private void bindAnimalFields(PreparedStatement ps, Animal a) throws SQLException {
        ps.setString(1, a.getName());
        ps.setString(2, a.getSpecies().name());
        ps.setString(3, a.getBreed());
        ps.setInt(4, a.getAge());
        ps.setString(5, a.getGender() != null ? a.getGender().name() : null);

        if (a.getWeight() != null) ps.setBigDecimal(6, a.getWeight()); else ps.setNull(6, Types.DECIMAL);

        ps.setString(7, a.getColor());
        ps.setString(8, a.getDescription());
        ps.setString(9, a.getStatus().name());

        if (a.getArrivalDate() != null) ps.setDate(10, Date.valueOf(a.getArrivalDate()));
        else ps.setNull(10, Types.DATE);

        // species-specific fields — only one is non-null per row
        setNullableBool(ps, 11, a instanceof Dog  ? ((Dog) a).isTrained() : null);
        setNullableBool(ps, 12, a instanceof Cat  ? ((Cat) a).isIndoor()  : null);
        setNullableBool(ps, 13, a instanceof Bird ? ((Bird) a).canFly()   : null);
    }

    private void setNullableBool(PreparedStatement ps, int idx, Boolean value) throws SQLException {
        if (value == null) ps.setNull(idx, Types.BOOLEAN);
        else ps.setBoolean(idx, value);
    }

    private Animal mapRow(ResultSet rs) throws SQLException {
        Species species = Species.valueOf(rs.getString("species"));

        String name        = rs.getString("name");
        String breed       = rs.getString("breed");
        int age            = rs.getInt("age");
        String genderStr   = rs.getString("gender");
        Gender gender      = genderStr != null ? Gender.valueOf(genderStr) : null;
        BigDecimal weight  = rs.getBigDecimal("weight");
        String color       = rs.getString("color");
        String description = rs.getString("description");

        Animal animal;
        switch (species) {
            case DOG:
                Boolean trained = getNullableBool(rs, "is_trained");
                animal = new Dog(name, breed, age, gender, weight, color, description,
                        trained != null && trained);
                break;
            case CAT:
                Boolean indoor = getNullableBool(rs, "is_indoor");
                animal = new Cat(name, breed, age, gender, weight, color, description,
                        indoor != null && indoor);
                break;
            case BIRD:
                Boolean flies = getNullableBool(rs, "can_fly");
                animal = new Bird(name, breed, age, gender, weight, color, description,
                        flies != null && flies);
                break;
            default:
                throw new SQLException("Unknown species in DB: " + species);
        }

        animal.setId(rs.getInt("id"));
        animal.setStatus(AnimalStatus.valueOf(rs.getString("status")));
        Date arrival = rs.getDate("arrival_date");
        if (arrival != null) {
            animal.setArrivalDate(arrival.toLocalDate());
        }
        return animal;
    }

    private Boolean getNullableBool(ResultSet rs, String col) throws SQLException {
        boolean b = rs.getBoolean(col);
        return rs.wasNull() ? null : b;
    }
}