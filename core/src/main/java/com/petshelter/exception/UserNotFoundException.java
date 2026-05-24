package com.petshelter.exception;


public class UserNotFoundException extends ShelterException {
    public UserNotFoundException(int id) {
        super("User not found with id: " + id);
    }

    public UserNotFoundException(String username) {
        super("User not found with username: " + username);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}