package com.petshelter.repository;

import com.petshelter.db.Database;
import com.petshelter.enums.UserRole;
import com.petshelter.exception.DuplicateUserException;
import com.petshelter.exception.ShelterException;
import com.petshelter.exception.UserNotFoundException;
import com.petshelter.model.Admin;
import com.petshelter.model.Client;
import com.petshelter.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// JDBC repository for User entities.
public class UserRepository implements Repository<User, Integer> {
    private static final String SQL_INSERT =
            "INSERT INTO users (username, password, full_name, email, phone, role) " +
                    "VALUES (?, ?, ?, ?, ?, ?) RETURNING id, created_at";

    private static final String SQL_UPDATE =
            "UPDATE users SET username = ?, password = ?, full_name = ?, " +
                    "email = ?, phone = ?, role = ? WHERE id = ?";

    private static final String SQL_DELETE = "DELETE FROM users WHERE id = ?";

    private static final String SQL_FIND_BY_ID =
            "SELECT id, username, password, full_name, email, phone, role, created_at " +
                    "FROM users WHERE id = ?";

    private static final String SQL_FIND_BY_USERNAME =
            "SELECT id, username, password, full_name, email, phone, role, created_at " +
                    "FROM users WHERE username = ?";

    private static final String SQL_FIND_BY_EMAIL =
            "SELECT id, username, password, full_name, email, phone, role, created_at " +
                    "FROM users WHERE email = ?";

    private static final String SQL_FIND_BY_ROLE =
            "SELECT id, username, password, full_name, email, phone, role, created_at " +
                    "FROM users WHERE role = ? ORDER BY full_name";

    private static final String SQL_FIND_ALL =
            "SELECT id, username, password, full_name, email, phone, role, created_at " +
                    "FROM users ORDER BY id";

    private static final String SQL_EXISTS = "SELECT 1 FROM users WHERE id = ?";
    private static final String SQL_COUNT = "SELECT COUNT(*) FROM users";

    // CREATE
    @Override
    public User save(User user) throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getRole().name());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getInt("id"));
                    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
            }
            return user;

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                String msg = e.getMessage().toLowerCase();
                if (msg.contains("username")) {
                    throw new DuplicateUserException("username", user.getUsername());
                } else if (msg.contains("email")) {
                    throw new DuplicateUserException("email", user.getEmail());
                }
                throw new DuplicateUserException(e.getMessage());
            }
            throw new ShelterException("Failed to save user", e);
        }
    }

    // UPDATE
    @Override
    public User update(User user) throws ShelterException {
        if (user.getId() == null) {
            throw new ShelterException("Cannot update user without id");
        }
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getRole().name());
            ps.setInt(7, user.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new UserNotFoundException(user.getId());
            }
            return user;

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicateUserException(e.getMessage());
            }
            throw new ShelterException("Failed to update user", e);
        }
    }

    @Override
    public Optional<User> findById(Integer id) throws ShelterException {
        return findBy(SQL_FIND_BY_ID, ps -> ps.setInt(1, id));
    }

    public Optional<User> findByUsername(String username) throws ShelterException {
        return findBy(SQL_FIND_BY_USERNAME, ps -> ps.setString(1, username));
    }

    public Optional<User> findByEmail(String email) throws ShelterException {
        return findBy(SQL_FIND_BY_EMAIL, ps -> ps.setString(1, email));
    }

    public List<User> findByRole(UserRole role) throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ROLE)) {
            ps.setString(1, role.name());
            return collect(ps);
        } catch (SQLException e) {
            throw new ShelterException("Failed to query users by role", e);
        }
    }

    @Override
    public List<User> findAll() throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL)) {
            return collect(ps);
        } catch (SQLException e) {
            throw new ShelterException("Failed to query users", e);
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
            throw new ShelterException("Failed to delete user " + id, e);
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
            throw new ShelterException("Failed to check user existence", e);
        }
    }

    @Override
    public long count() throws ShelterException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new ShelterException("Failed to count users", e);
        }
    }


    // PRIVATE HELPERS
    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private Optional<User> findBy(String sql, StatementBinder binder) throws ShelterException {
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

    private List<User> collect(PreparedStatement ps) throws SQLException {
        List<User> result = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        String role = rs.getString("role");

        String username = rs.getString("username");
        String password = rs.getString("password");
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");

        User user = "ADMIN".equals(role)
                ? new Admin(username, password, fullName, email, phone)
                : new Client(username, password, fullName, email, phone);

        user.setId(rs.getInt("id"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            user.setCreatedAt(ts.toLocalDateTime());
        }
        return user;
    }
}