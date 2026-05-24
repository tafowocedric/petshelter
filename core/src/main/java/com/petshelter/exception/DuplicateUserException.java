package com.petshelter.exception;


public class DuplicateUserException extends ShelterException {
    public DuplicateUserException(String field, String value) {
        super("User with " + field + " '" + value + "' already exists");
    }

    public DuplicateUserException(String message) {
        super(message);
    }
}