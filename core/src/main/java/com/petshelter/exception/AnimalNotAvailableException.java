package com.petshelter.exception;


public class AnimalNotAvailableException extends ShelterException {
    public AnimalNotAvailableException(int animalId, String currentStatus) {
        super("Животное " + animalId + " недоступно для усыновления (статус: " + currentStatus + ")");
    }

    public AnimalNotAvailableException(String message) {
        super(message);
    }
}