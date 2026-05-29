package com.petshelter.exception;

// Base exception for all application-specific errors.
public class ShelterException extends Exception {
    public ShelterException(String message) {
        super(message);
    }

    public ShelterException(String message, Throwable cause) {
        super(message, cause);
    }
}