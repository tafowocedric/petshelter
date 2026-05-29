package com.petshelter.exception;


public class ValidationException extends ShelterException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String field, String reason) {
        super("Ошибка валидации поля '" + field + "': " + reason);
    }
}