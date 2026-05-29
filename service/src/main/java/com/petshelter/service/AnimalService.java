package com.petshelter.service;

import com.petshelter.enums.AnimalStatus;
import com.petshelter.enums.Species;
import com.petshelter.exception.AnimalNotFoundException;
import com.petshelter.exception.ShelterException;
import com.petshelter.exception.ValidationException;
import com.petshelter.model.Animal;
import com.petshelter.model.User;
import com.petshelter.repository.AnimalRepository;

import java.util.List;
import java.util.Optional;

// Business logic for Animal management.
public class AnimalService {
    private final AnimalRepository animalRepo;
    private final AuthService authService;

    public AnimalService(AnimalRepository animalRepo, AuthService authService) {
        this.animalRepo = animalRepo;
        this.authService = authService;
    }

    // CREATE  (admin only)
    public Animal create(User actor, Animal animal) throws ShelterException {
        authService.requireAdmin(actor, "create animal");
        validate(animal);
        return animalRepo.save(animal);
    }

    // UPDATE  (admin only)
    public Animal update(User actor, Animal animal) throws ShelterException {
        authService.requireAdmin(actor, "update animal");
        if (animal.getId() == null) {
            throw new ValidationException("id", "Нельзя обновить животное без id");
        }
        validate(animal);
        return animalRepo.update(animal);
    }

    // DELETE  (admin only)
    public boolean delete(User actor, int animalId) throws ShelterException {
        authService.requireAdmin(actor, "delete animal");
        if (!animalRepo.existsById(animalId)) {
            throw new AnimalNotFoundException(animalId);
        }
        return animalRepo.deleteById(animalId);
    }

    // STATUS TRANSITIONS  (admin only — used by AdoptionService)
    public Animal markPending(User actor, int animalId) throws ShelterException {
        authService.requireAdmin(actor, "mark animal pending");
        return transition(animalId, AnimalStatus.AVAILABLE, AnimalStatus.PENDING);
    }

    public Animal markAdopted(User actor, int animalId) throws ShelterException {
        authService.requireAdmin(actor, "mark animal adopted");
        return transition(animalId, AnimalStatus.PENDING, AnimalStatus.ADOPTED);
    }

    public Animal markAvailable(User actor, int animalId) throws ShelterException {
        authService.requireAdmin(actor, "mark animal available");
        Animal animal = requireExisting(animalId);
        animal.setStatus(AnimalStatus.AVAILABLE);
        return animalRepo.update(animal);
    }

    // READ
    public Animal getById(int id) throws ShelterException {
        return requireExisting(id);
    }

    public List<Animal> getAll() throws ShelterException {
        return animalRepo.findAll();
    }

    public List<Animal> getAvailable() throws ShelterException {
        return animalRepo.findAvailable();
    }

    public long countByStatus(AnimalStatus status) throws ShelterException {
        return animalRepo.countByStatus(status);
    }

    // SEARCH
    public Optional<Animal> search(String name) throws ShelterException {
        return animalRepo.findByName(name);
    }

    public List<Animal> search(Species species) throws ShelterException {
        return animalRepo.findBySpecies(species);
    }

    public List<Animal> search(AnimalStatus status) throws ShelterException {
        return animalRepo.findByStatus(status);
    }

    public List<Animal> search(Species species, AnimalStatus status) throws ShelterException {
        return animalRepo.findBySpeciesAndStatus(species, status);
    }

    // PRIVATE HELPERS
    private void validate(Animal animal) throws ValidationException {
        if (animal == null) {
            throw new ValidationException("Животное не может быть null");
        }
        if (animal.getName() == null || animal.getName().isBlank()) {
            throw new ValidationException("имя", "Обязательное поле");
        }
        if (animal.getSpecies() == null) {
            throw new ValidationException("вид", "Обязательное поле");
        }
        if (animal.getAge() < 0) {
            throw new ValidationException("возраст", "Не может быть отрицательным");
        }
        if (animal.getWeight() != null && animal.getWeight().signum() < 0) {
            throw new ValidationException("вес", "Не может быть отрицательным");
        }
    }

    private Animal requireExisting(int id) throws ShelterException {
        return animalRepo.findById(id).orElseThrow(() -> new AnimalNotFoundException(id));
    }

    private Animal transition(int animalId, AnimalStatus expected, AnimalStatus next) throws ShelterException {
        Animal animal = requireExisting(animalId);
        if (animal.getStatus() != expected) {
            throw new ValidationException("статус", "Ожидался " + expected + ", текущий: " + animal.getStatus());
        }
        animal.setStatus(next);
        return animalRepo.update(animal);
    }
}