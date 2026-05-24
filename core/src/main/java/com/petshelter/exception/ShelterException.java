package com.petshelter.exception;

/**
 * Base exception for all application-specific errors.
 * [EXCEPTIONS] [INHERITANCE] — extended by more specific exceptions
 * This forces explicit error handling at service boundaries.
 */
public class ShelterException extends Exception {
    public ShelterException(String message) {
        super(message);
    }

    public ShelterException(String message, Throwable cause) {
        super(message, cause);
    }
}