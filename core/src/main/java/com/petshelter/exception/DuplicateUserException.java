package com.petshelter.exception;


public class DuplicateUserException extends ShelterException {
    public DuplicateUserException(String field, String value) {
        super("Пользователь с " + field + " '" + value + "' уже существует");
    }

    public DuplicateUserException(String message) {
        super(message);
    }
}