package com.petshelter.model;

import com.petshelter.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Objects;

// Base class for all users.
public abstract class User {
    private Integer id;
    private String username;
    private String password;        // stored hashed
    private String fullName;
    private String email;
    private String phone;
    private LocalDateTime createdAt;

    protected User(String username, String password, String fullName, String email, String phone) {
        setUsername(username);
        this.password = Objects.requireNonNull(password, "Пароль не может быть null");

        setFullName(fullName);
        setEmail(email);
        this.phone = phone;
        this.createdAt = LocalDateTime.now();
    }

    public abstract UserRole getRole();

    public String getDisplayName() {
        return fullName;
    }

    public String getDisplayName(boolean withUsername) {
        return withUsername ? fullName + " (" + username + ")" : fullName;
    }

    // Getters & setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) {
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Имя пользователя должно содержать не менее 3 символов");
        }
        this.username = username.trim();
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Полное имя не может быть пустым");
        }
        this.fullName = fullName.trim();
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Неверный email");
        }
        this.email = email.trim().toLowerCase();
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return Objects.equals(id, ((User) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getRole() + ": " + getDisplayName(true);
    }
}