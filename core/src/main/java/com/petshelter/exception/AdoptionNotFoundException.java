package com.petshelter.exception;


public class AdoptionNotFoundException extends ShelterException {
    public AdoptionNotFoundException(int id) {
        super("Adoption not found with id: " + id);
    }

    public AdoptionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}