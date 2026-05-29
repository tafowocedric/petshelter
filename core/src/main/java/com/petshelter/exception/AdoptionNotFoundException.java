package com.petshelter.exception;


public class AdoptionNotFoundException extends ShelterException {
    public AdoptionNotFoundException(int id) {
        super("Заявка на усыновление не найдена: id=" + id);
    }

    public AdoptionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}