package com.petshelter.exception;


public class AnimalNotFoundException extends ShelterException {
    public AnimalNotFoundException(int id) {
        super("Животное не найдено: id=" + id);
    }

    public AnimalNotFoundException(String name) {
        super("Животное не найдено: имя=" + name);
    }

    public AnimalNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}