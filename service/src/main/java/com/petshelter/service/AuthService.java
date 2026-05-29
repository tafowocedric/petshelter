package com.petshelter.service;

import com.petshelter.exception.DuplicateUserException;
import com.petshelter.exception.InvalidCredentialsException;
import com.petshelter.exception.ShelterException;
import com.petshelter.exception.UnauthorizedActionException;
import com.petshelter.exception.UserNotFoundException;
import com.petshelter.exception.ValidationException;
import com.petshelter.model.Admin;
import com.petshelter.model.Client;
import com.petshelter.model.User;
import com.petshelter.repository.UserRepository;
import com.petshelter.util.PasswordHasher;

import java.util.Optional;
import java.util.regex.Pattern;

// Authentication and user-registration service.
public class AuthService implements Authenticator {
    private static final Pattern EMAIL_RE = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepository userRepo;

    public AuthService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // LOGIN
    @Override
    public User login(String username, String password) throws ShelterException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        Optional<User> found = userRepo.findByUsername(username.trim());
        if (found.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        User user = found.get();
        if (!PasswordHasher.verify(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }

    // REGISTER
    @Override
    public Client register(String username, String plainPassword, String fullName, String email, String phone) throws ShelterException {
        validateRegistration(username, plainPassword, fullName, email);

        // Pre-check for cleaner error messages (the DB unique constraint would also catch this)
        if (userRepo.findByUsername(username).isPresent()) {
            throw new DuplicateUserException("username", username);
        }
        if (userRepo.findByEmail(email).isPresent()) {
            throw new DuplicateUserException("email", email);
        }

        String hashed = PasswordHasher.hash(plainPassword);
        Client client = new Client(username.trim(), hashed, fullName.trim(), email.trim().toLowerCase(), phone);
        return (Client) userRepo.save(client);
    }

    public Admin createAdmin(User actor, String username, String plainPassword, String fullName, String email, String phone) throws ShelterException {
        requireAdmin(actor, "create admin account");
        return createAdmin(username, plainPassword, fullName, email, phone);
    }


    Admin createAdmin(String username, String plainPassword, String fullName, String email, String phone) throws ShelterException {
        validateRegistration(username, plainPassword, fullName, email);
        String hashed = PasswordHasher.hash(plainPassword);

        Admin admin = new Admin(username.trim(), hashed, fullName.trim(), email.trim().toLowerCase(), phone);
        return (Admin) userRepo.save(admin);
    }

    // PASSWORD CHANGE
    public void changePassword(User user, String oldPassword, String newPassword) throws ShelterException {
        if (!PasswordHasher.verify(oldPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Текущий пароль неверен");
        }
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("пароль", "Минимум " + MIN_PASSWORD_LENGTH + " символов");
        }
        user.setPassword(PasswordHasher.hash(newPassword));
        userRepo.update(user);
    }

    // AUTHORIZATION HELPERS
    public void requireAdmin(User actor, String action) throws UnauthorizedActionException {
        if (actor == null) {
            throw new UnauthorizedActionException(action);
        }
        if (!(actor instanceof Admin)) {
            throw new UnauthorizedActionException(action, actor.getRole().name());
        }
    }

    public boolean isAdmin(User actor) {
        return actor instanceof Admin;
    }

    // VALIDATION
    private void validateRegistration(String username, String password, String fullName, String email) throws ValidationException {
        if (username == null || username.trim().length() < 3) {
            throw new ValidationException("имя пользователя", "Минимум 3 символа");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("пароль", "Минимум " + MIN_PASSWORD_LENGTH + " символов");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("полное имя", "Не может быть пустым");
        }
        if (email == null || !EMAIL_RE.matcher(email.trim()).matches()) {
            throw new ValidationException("email", "Неверный формат email");
        }
    }

    public Optional<User> findUser(String username) throws ShelterException {
        return userRepo.findByUsername(username);
    }

    public UserRepository getUserRepository() {
        return userRepo;
    }

    public boolean userExists(String username) throws ShelterException {
        return userRepo.findByUsername(username).isPresent();
    }

    public User getById(int id) throws ShelterException {
        return userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }
}