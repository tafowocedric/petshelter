package com.petshelter.exception;


public class AnimalNotFoundException extends ShelterException {
    public AnimalNotFoundException(int id) {
        super("Animal not found with id: " + id);
    }

    public AnimalNotFoundException(String name) {
        super("Animal not found with name: " + name);
    }

    public AnimalNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}