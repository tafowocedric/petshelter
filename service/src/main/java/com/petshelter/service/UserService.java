package com.petshelter.service;

import com.petshelter.enums.UserRole;
import com.petshelter.exception.ShelterException;
import com.petshelter.exception.UnauthorizedActionException;
import com.petshelter.exception.UserNotFoundException;
import com.petshelter.exception.ValidationException;
import com.petshelter.model.Admin;
import com.petshelter.model.User;
import com.petshelter.repository.UserRepository;

import java.util.List;

/**
 * Business logic for User management — used by the admin dashboard to view, edit, and remove client accounts.
 * [POLYMORPHISM] [EXCEPTIONS]
 */
public class UserService {
    private final UserRepository userRepo;
    private final AuthService authService;

    public UserService(UserRepository userRepo, AuthService authService) {
        this.userRepo = userRepo;
        this.authService = authService;
    }

    // READ
    public User getById(int id) throws ShelterException {
        return userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public List<User> getAll(User actor) throws ShelterException {
        authService.requireAdmin(actor, "view all users");
        return userRepo.findAll();
    }

    public List<User> getAllClients(User actor) throws ShelterException {
        authService.requireAdmin(actor, "view clients");
        return userRepo.findByRole(UserRole.CLIENT);
    }

    public List<User> getAllAdmins(User actor) throws ShelterException {
        authService.requireAdmin(actor, "view admins");
        return userRepo.findByRole(UserRole.ADMIN);
    }

    // UPDATE
    public User updateProfile(User actor, User updated) throws ShelterException {
        if (updated.getId() == null) {
            throw new ValidationException("id", "Cannot update user without id");
        }

        requireSelfOrAdmin(actor, updated.getId(), "update profile");

        User original = getById(updated.getId());
        original.setFullName(updated.getFullName());
        original.setEmail(updated.getEmail());
        original.setPhone(updated.getPhone());

        return userRepo.update(original);
    }

    // DELETE
    public boolean delete(User actor, int targetUserId) throws ShelterException {
        authService.requireAdmin(actor, "delete user");

        if (actor.getId() != null && actor.getId() == targetUserId) {
            throw new UnauthorizedActionException("delete your own admin account");
        }

        if (!userRepo.existsById(targetUserId)) {
            throw new UserNotFoundException(targetUserId);
        }
        return userRepo.deleteById(targetUserId);
    }

    // STATS
    public long countAll() throws ShelterException {
        return userRepo.count();
    }

    public long countClients() throws ShelterException {
        return userRepo.findByRole(UserRole.CLIENT).size();
    }

    public long countAdmins() throws ShelterException {
        return userRepo.findByRole(UserRole.ADMIN).size();
    }

    // PRIVATE HELPERS
    private void requireSelfOrAdmin(User actor, int targetId, String action) throws UnauthorizedActionException {
        if (actor == null) {
            throw new UnauthorizedActionException(action);
        }

        boolean isSelf = actor.getId() != null && actor.getId() == targetId;
        boolean isAdmin = actor instanceof Admin;
        if (!isSelf && !isAdmin) {
            throw new UnauthorizedActionException(action, actor.getRole().name());
        }
    }
}