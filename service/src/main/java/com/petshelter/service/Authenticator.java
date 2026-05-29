package com.petshelter.service;

import com.petshelter.exception.ShelterException;
import com.petshelter.model.Client;
import com.petshelter.model.User;

/**
 * Authentication contract.
 * [INTERFACES] — service-layer abstraction over user auth
 */
public interface Authenticator {
    User login(String username, String password) throws ShelterException;
    Client register(String username, String plainPassword, String fullName, String email, String phone) throws ShelterException;
}