package com.petshelter.exception;


public class InvalidCredentialsException extends ShelterException {
    public InvalidCredentialsException() {
        super("Invalid username or password");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}