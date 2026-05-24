package com.petshelter.exception;


public class AnimalNotAvailableException extends ShelterException {
    public AnimalNotAvailableException(int animalId, String currentStatus) {
        super("Animal " + animalId + " is not available for adoption (status: " + currentStatus + ")");
    }

    public AnimalNotAvailableException(String message) {
        super(message);
    }
}