package com.petshelter.exception;


public class DuplicateAdoptionException extends ShelterException {
    public DuplicateAdoptionException(int animalId, int clientId) {
        super("Заявка на усыновление уже существует для животного " + animalId + " и клиента " + clientId);
    }

    public DuplicateAdoptionException(String message) {
        super(message);
    }
}