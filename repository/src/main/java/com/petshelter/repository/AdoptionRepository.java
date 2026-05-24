package com.petshelter.repository;

import com.petshelter.db.Database;
import com.petshelter.enums.AdoptionStatus;
import com.petshelter.enums.AnimalStatus;
import com.petshelter.enums.Gender;
import com.petshelter.enums.Species;
import com.petshelter.enums.UserRole;
import com.petshelter.exception.AdoptionNotFoundException;
import com.petshelter.exception.ShelterException;
import com.petshelter.model.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC repository for Adoption entities.
 * [INTERFACES] [POLYMORPHISM] [EXCEPTIONS]
 *
 * SQL joins fetch the related Animal and Client in a single query.
 */
public class AdoptionRepository implements Repository<Adoption, Integer> {
    // Base SQL fragments (kept consistent across queries)
    private static final String ADOPTION_COLS =
        "a.id AS adoption_id, a.animal_id AS animal_id, a.client_id AS client_id, a.adoption_date AS adoption_date, " +
        "a.status AS adoption_status, a.notes AS notes, a.approved_by AS approved_by";

    private static final String ANIMAL_COLS =
        "an.id AS an_id, an.name AS an_name, an.species AS an_species, an.breed AS an_breed, an.age AS an_age, " +
        "an.gender AS an_gender, an.weight AS an_weight, an.color AS an_color, an.description AS an_description, " +
        "an.status AS an_status, an.arrival_date AS an_arrival_date, an.is_trained AS an_is_trained, " +
        "an.is_indoor AS an_is_indoor, an.can_fly AS an_can_fly";

    private static final String CLIENT_COLS =
        "u.id AS u_id, u.username AS u_username, u.password AS u_password, u.full_name AS u_full_name, " +
        "u.email AS u_email, u.phone AS u_phone, u.role AS u_role, u.created_at AS u_created_at";

    private static final String SELECT_JOIN =
        "SELECT " + ADOPTION_COLS + ", " + ANIMAL_COLS + ", " + CLIENT_COLS + " " +
        "FROM adoptions a " +
        "JOIN animals an ON an.id = a.animal_id " +
        "JOIN users   u  ON u.id  = a.client_id ";

    // Insert / update / delete don't need joins
    private static final String SQL_INSERT =
        "INSERT INTO adoptions (animal_id, client_id, adoption_date, status, notes, approved_by) " +
            "VALUES (?, ?, ?, ?, ?, ?) RETURNING id, adoption_date";

    private static final String SQL_UPDATE =
        "UPDATE adoptions SET animal_id = ?, client_id = ?, adoption_date = ?, " +
            "status = ?, notes = ?, approved_by = ? WHERE id = ?";

    private static final String SQL_DELETE = "DELETE FROM adoptions WHERE id = ?";
    private static final String SQL_EXISTS = "SELECT 1 FROM adoptions WHERE id = ?";
    private static final String SQL_COUNT  = "SELECT COUNT(*) FROM adoptions";

    // Filters for the joined queries
    private static final String SQL_FIND_BY_ID = SELECT_JOIN + "WHERE a.id = ?";
    private static final String SQL_FIND_ALL = SELECT_JOIN + "ORDER BY a.adoption_date DESC, a.id DESC";
    private static final String SQL_FIND_BY_STATUS = SELECT_JOIN + "WHERE a.status = ? ORDER BY a.adoption_date DESC";
    private static final String SQL_FIND_BY_CLIENT = SELECT_JOIN + "WHERE a.client_id = ? ORDER BY a.adoption_date DESC";
    private static final String SQL_FIND_BY_ANIMAL = SELECT_JOIN + "WHERE a.animal_id = ? ORDER BY a.adoption_date DESC";
    private static final String SQL_EXISTS_ACTIVE_FOR_ANIMAL =
        "SELECT 1 FROM adoptions WHERE animal_id = ? AND status IN ('PENDING', 'APPROVED', 'COMPLETED')";

    // CREATE
    @Override
    public Adoption save(Adoption adoption) throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {

            ps.setInt(1, adoption.getAnimalId());
            ps.setInt(2, adoption.getClientId());
            ps.setDate(3, adoption.getAdoptionDate() != null
                    ? Date.valueOf(adoption.getAdoptionDate()) : Date.valueOf(java.time.LocalDate.now()));
            ps.setString(4, adoption.getStatus().name());
            ps.setString(5, adoption.getNotes());

            if (adoption.getApprovedBy() != null) ps.setInt(6, adoption.getApprovedBy());
            else ps.setNull(6, Types.INTEGER);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    adoption.setId(rs.getInt("id"));
                    adoption.setAdoptionDate(rs.getDate("adoption_date").toLocalDate());
                }
            }
            return adoption;

        } catch (SQLException e) {
            throw new ShelterException("Failed to save adoption", e);
        }
    }

    // UPDATE
    @Override
    public Adoption update(Adoption adoption) throws ShelterException {
        if (adoption.getId() == null) {
            throw new ShelterException("Cannot update adoption without id");
        }
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            ps.setInt(1, adoption.getAnimalId());
            ps.setInt(2, adoption.getClientId());
            ps.setDate(3, Date.valueOf(adoption.getAdoptionDate()));
            ps.setString(4, adoption.getStatus().name());
            ps.setString(5, adoption.getNotes());

            if (adoption.getApprovedBy() != null) ps.setInt(6, adoption.getApprovedBy());
            else ps.setNull(6, Types.INTEGER);

            ps.setInt(7, adoption.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new AdoptionNotFoundException(adoption.getId());
            }
            return adoption;

        } catch (SQLException e) {
            throw new ShelterException("Failed to update adoption", e);
        }
    }

    // READ
    @Override
    public Optional<Adoption> findById(Integer id) throws ShelterException {
        return findJoinedById(id).map(JoinedAdoption::getAdoption);
    }

    @Override
    public List<Adoption> findAll() throws ShelterException {
        List<Adoption> result = new ArrayList<>();
        for (JoinedAdoption ja : findAllJoined()) {
            result.add(ja.getAdoption());
        }
        return result;
    }

    public List<Adoption> findByClient(int clientId) throws ShelterException {
        return findManyJoined(SQL_FIND_BY_CLIENT, ps -> ps.setInt(1, clientId))
            .stream().map(JoinedAdoption::getAdoption).collect(java.util.stream.Collectors.toList());
    }

    public List<Adoption> findByStatus(AdoptionStatus status) throws ShelterException {
        return findManyJoined(SQL_FIND_BY_STATUS, ps -> ps.setString(1, status.name()))
            .stream().map(JoinedAdoption::getAdoption).collect(java.util.stream.Collectors.toList());
    }

    // READ — joined adoptions (with hydrated Animal + Client)

    /** Returns the adoption together with its joined animal and client. */
    public Optional<JoinedAdoption> findJoinedById(int id) throws ShelterException {
        return findJoined(SQL_FIND_BY_ID, ps -> ps.setInt(1, id));
    }

    public List<JoinedAdoption> findAllJoined() throws ShelterException {
        return findManyJoined(SQL_FIND_ALL, ps -> {});
    }

    public List<JoinedAdoption> findJoinedByClient(int clientId) throws ShelterException {
        return findManyJoined(SQL_FIND_BY_CLIENT, ps -> ps.setInt(1, clientId));
    }

    public List<JoinedAdoption> findJoinedByStatus(AdoptionStatus status) throws ShelterException {
        return findManyJoined(SQL_FIND_BY_STATUS, ps -> ps.setString(1, status.name()));
    }

    public List<JoinedAdoption> findJoinedByAnimal(int animalId) throws ShelterException {
        return findManyJoined(SQL_FIND_BY_ANIMAL, ps -> ps.setInt(1, animalId));
    }

    // BUSINESS CHECK
    /** True if this animal already has a non-rejected adoption record. */
    public boolean hasActiveAdoption(int animalId) throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_EXISTS_ACTIVE_FOR_ANIMAL)) {
            ps.setInt(1, animalId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ShelterException("Failed to check active adoption", e);
        }
    }

    // DELETE / UTIL
    @Override
    public boolean deleteById(Integer id) throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ShelterException("Failed to delete adoption " + id, e);
        }
    }

    @Override
    public boolean existsById(Integer id) throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_EXISTS)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ShelterException("Failed to check adoption existence", e);
        }
    }

    @Override
    public long count() throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new ShelterException("Failed to count adoptions", e);
        }
    }

    // PRIVATE HELPERS

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private Optional<JoinedAdoption> findJoined(String sql, StatementBinder binder) throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapJoinedRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ShelterException("Query failed", e);
        }
    }

    private List<JoinedAdoption> findManyJoined(String sql, StatementBinder binder) throws ShelterException {
        List<JoinedAdoption> out = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapJoinedRow(rs));
                }
            }
            return out;
        } catch (SQLException e) {
            throw new ShelterException("Query failed", e);
        }
    }

    /**
     * Maps one row of the joined query into an Adoption + Animal + User triple.
     * Uses the same polymorphic mapping ideas as the other repositories.
     */
    private JoinedAdoption mapJoinedRow(ResultSet rs) throws SQLException {
        // ---- Adoption ----
        Adoption adoption = new Adoption(rs.getInt("animal_id"), rs.getInt("client_id"));

        adoption.setId(rs.getInt("adoption_id"));
        adoption.setAdoptionDate(rs.getDate("adoption_date").toLocalDate());
        adoption.setStatus(AdoptionStatus.valueOf(rs.getString("adoption_status")));
        adoption.setNotes(rs.getString("notes"));

        int approvedBy = rs.getInt("approved_by");
        if (!rs.wasNull()) {
            adoption.setApprovedBy(approvedBy);
        }

        // ---- Animal ----
        Animal animal = mapAnimalFromJoin(rs);

        // ---- Client ----
        User client = mapUserFromJoin(rs);

        return new JoinedAdoption(adoption, animal, client);
    }

    private Animal mapAnimalFromJoin(ResultSet rs) throws SQLException {
        Species species = Species.valueOf(rs.getString("an_species"));
        String name        = rs.getString("an_name");
        String breed       = rs.getString("an_breed");
        int age            = rs.getInt("an_age");
        String genderStr   = rs.getString("an_gender");
        Gender gender      = genderStr != null ? Gender.valueOf(genderStr) : null;
        BigDecimal weight  = rs.getBigDecimal("an_weight");
        String color       = rs.getString("an_color");
        String description = rs.getString("an_description");

        Animal animal;
        switch (species) {
            case DOG:
                Boolean trained = getNullableBool(rs, "an_is_trained");
                animal = new Dog(name, breed, age, gender, weight, color, description, trained != null && trained);
                break;
            case CAT:
                Boolean indoor = getNullableBool(rs, "an_is_indoor");
                animal = new Cat(name, breed, age, gender, weight, color, description, indoor != null && indoor);
                break;
            case BIRD:
                Boolean flies = getNullableBool(rs, "an_can_fly");
                animal = new Bird(name, breed, age, gender, weight, color, description, flies != null && flies);
                break;
            default:
                throw new SQLException("Unknown species: " + species);
        }

        animal.setId(rs.getInt("an_id"));
        animal.setStatus(AnimalStatus.valueOf(rs.getString("an_status")));
        Date arrival = rs.getDate("an_arrival_date");

        if (arrival != null) {
            animal.setArrivalDate(arrival.toLocalDate());
        }

        return animal;
    }

    private User mapUserFromJoin(ResultSet rs) throws SQLException {
        String role     = rs.getString("u_role");
        String username = rs.getString("u_username");
        String password = rs.getString("u_password");
        String fullName = rs.getString("u_full_name");
        String email    = rs.getString("u_email");
        String phone    = rs.getString("u_phone");

        User user = UserRole.ADMIN.name().equals(role)
                ? new Admin(username, password, fullName, email, phone)
                : new Client(username, password, fullName, email, phone);

        user.setId(rs.getInt("u_id"));
        Timestamp ts = rs.getTimestamp("u_created_at");

        if (ts != null) user.setCreatedAt(ts.toLocalDateTime());
        return user;
    }

    private Boolean getNullableBool(ResultSet rs, String col) throws SQLException {
        boolean b = rs.getBoolean(col);
        return rs.wasNull() ? null : b;
    }
}
