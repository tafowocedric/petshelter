package com.petshelter.exception;


public class UnauthorizedActionException extends ShelterException {
    public UnauthorizedActionException(String action) {
        super("Нет прав для действия: " + action);
    }

    public UnauthorizedActionException(String action, String role) {
        super("Роль " + role + " не имеет прав для: " + action);
    }
}