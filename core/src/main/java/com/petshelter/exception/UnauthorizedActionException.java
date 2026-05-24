package com.petshelter.exception;


public class UnauthorizedActionException extends ShelterException {
    public UnauthorizedActionException(String action) {
        super("Unauthorized action: " + action);
    }

    public UnauthorizedActionException(String action, String role) {
        super("Role " + role + " is not authorized to: " + action);
    }
}