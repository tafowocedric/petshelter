package com.petshelter.exception;


public class UserNotFoundException extends ShelterException {
    public UserNotFoundException(int id) {
        super("Пользователь не найден: id=" + id);
    }

    public UserNotFoundException(String username) {
        super("Пользователь не найден: имя=" + username);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}