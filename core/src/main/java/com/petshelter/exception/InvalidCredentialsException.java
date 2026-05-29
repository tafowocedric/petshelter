package com.petshelter.exception;


public class InvalidCredentialsException extends ShelterException {
    public InvalidCredentialsException() {
        super("Неверное имя пользователя или пароль");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}