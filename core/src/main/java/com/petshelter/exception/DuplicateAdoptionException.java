package com.petshelter.exception;


public class DuplicateAdoptionException extends ShelterException {
    public DuplicateAdoptionException(int animalId, int clientId) {
        super("Adoption already exists for animal " + animalId + " and client " + clientId);
    }

    public DuplicateAdoptionException(String message) {
        super(message);
    }
}