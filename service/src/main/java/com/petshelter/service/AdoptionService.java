package com.petshelter.service;

import com.petshelter.enums.AdoptionStatus;
import com.petshelter.enums.AnimalStatus;
import com.petshelter.exception.AdoptionNotFoundException;
import com.petshelter.exception.AnimalNotAvailableException;
import com.petshelter.exception.DuplicateAdoptionException;
import com.petshelter.exception.ShelterException;
import com.petshelter.exception.UnauthorizedActionException;
import com.petshelter.model.Adoption;
import com.petshelter.model.Animal;
import com.petshelter.model.Client;
import com.petshelter.model.User;
import com.petshelter.repository.AdoptionRepository;
import com.petshelter.repository.AnimalRepository;
import com.petshelter.repository.JoinedAdoption;
import com.petshelter.repository.UserRepository;

import java.util.List;
import java.util.Optional;

// Adoption workflow orchestration.
public class AdoptionService {
    private final AdoptionRepository adoptionRepo;
    private final AnimalRepository animalRepo;
    private final UserRepository userRepo;
    private final AnimalService animalService;
    private final AuthService authService;

    public AdoptionService(AdoptionRepository adoptionRepo, AnimalRepository animalRepo, UserRepository userRepo,
           AnimalService animalService, AuthService authService) {
        this.adoptionRepo = adoptionRepo;
        this.animalRepo = animalRepo;
        this.userRepo = userRepo;
        this.animalService = animalService;
        this.authService = authService;
    }

    // REQUEST
    public Adoption request(User actor, int animalId) throws ShelterException {
        return request(actor, animalId, null);
    }

    public Adoption request(User actor, int animalId, String notes) throws ShelterException {
        if (!(actor instanceof Client)) {
            throw new UnauthorizedActionException("request adoption", actor.getRole().name());
        }

        // Verify the animal exists and is available
        Animal animal = animalRepo.findById(animalId).orElseThrow(
            () -> new com.petshelter.exception.AnimalNotFoundException(animalId));

        if (animal.getStatus() != AnimalStatus.AVAILABLE) {
            throw new AnimalNotAvailableException(animalId, animal.getStatus().name());
        }

        // Prevent duplicate active adoptions on the same animal
        if (adoptionRepo.hasActiveAdoption(animalId)) {
            throw new DuplicateAdoptionException(animalId, actor.getId());
        }

        Adoption adoption = new Adoption(animalId, actor.getId());
        adoption.setNotes(notes);
        adoption.setStatus(AdoptionStatus.PENDING);
        return adoptionRepo.save(adoption);
    }

    // APPROVE / REJECT / COMPLETE
    public Adoption approve(User actor, int adoptionId) throws ShelterException {
        authService.requireAdmin(actor, "approve adoption");
        Adoption adoption = requireExisting(adoptionId);

        if (adoption.getStatus() != AdoptionStatus.PENDING) {
            throw new ShelterException("Одобрить можно только заявки в статусе PENDING (текущий: " + adoption.getStatus() + ")");
        }

        adoption.setStatus(AdoptionStatus.APPROVED);
        adoption.setApprovedBy(actor.getId());
        Adoption updated = adoptionRepo.update(adoption);

        // Move the animal from AVAILABLE → PENDING
        animalService.markPending(actor, adoption.getAnimalId());
        return updated;
    }

    public Adoption reject(User actor, int adoptionId, String reason) throws ShelterException {
        authService.requireAdmin(actor, "reject adoption");
        Adoption adoption = requireExisting(adoptionId);

        if (adoption.getStatus() != AdoptionStatus.PENDING) {
            throw new ShelterException("Отклонить можно только заявки в статусе PENDING (текущий: " + adoption.getStatus() + ")");
        }

        adoption.setStatus(AdoptionStatus.REJECTED);
        adoption.setApprovedBy(actor.getId());

        if (reason != null && !reason.isBlank()) {
            String existing = adoption.getNotes() == null ? "" : adoption.getNotes() + "\n";
            adoption.setNotes(existing + "[Rejected] " + reason);
        }
        return adoptionRepo.update(adoption);
    }

    public Adoption complete(User actor, int adoptionId) throws ShelterException {
        authService.requireAdmin(actor, "complete adoption");
        Adoption adoption = requireExisting(adoptionId);
        if (adoption.getStatus() != AdoptionStatus.APPROVED) {
            throw new ShelterException("Завершить можно только заявки в статусе APPROVED (текущий: " + adoption.getStatus() + ")");
        }

        adoption.setStatus(AdoptionStatus.COMPLETED);
        Adoption updated = adoptionRepo.update(adoption);

        animalService.markAdopted(actor, adoption.getAnimalId());
        return updated;
    }

    public Adoption cancel(User actor, int adoptionId) throws ShelterException {
        Adoption adoption = requireExisting(adoptionId);

        // Either the client who created it, or an admin, can cancel
        boolean isOwner = adoption.getClientId().equals(actor.getId());
        boolean isAdmin = authService.isAdmin(actor);
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedActionException("cancel adoption", actor.getRole().name());
        }

        // If the animal was moved to PENDING because of this adoption, revert it
        if (adoption.getStatus() == AdoptionStatus.APPROVED) {
            animalService.markAvailable(actor, adoption.getAnimalId());
        }

        adoption.setStatus(AdoptionStatus.REJECTED);
        String existing = adoption.getNotes() == null ? "" : adoption.getNotes() + "\n";
        adoption.setNotes(existing + "[Cancelled by " + actor.getUsername() + "]");
        return adoptionRepo.update(adoption);
    }

    // READ
    public Adoption getById(int id) throws ShelterException {
        return requireExisting(id);
    }

    public Optional<JoinedAdoption> getJoinedById(int id) throws ShelterException {
        return adoptionRepo.findJoinedById(id);
    }

    public List<JoinedAdoption> getAllJoined(User actor) throws ShelterException {
        authService.requireAdmin(actor, "view all adoptions");
        return adoptionRepo.findAllJoined();
    }

    public List<JoinedAdoption> getPending(User actor) throws ShelterException {
        authService.requireAdmin(actor, "view pending adoptions");
        return adoptionRepo.findJoinedByStatus(AdoptionStatus.PENDING);
    }

    public List<JoinedAdoption> getMyAdoptions(User actor) throws ShelterException {
        if (actor == null || actor.getId() == null) {
            throw new UnauthorizedActionException("view own adoptions");
        }
        return adoptionRepo.findJoinedByClient(actor.getId());
    }

    // STATISTICS
    public long countAll() throws ShelterException {
        return adoptionRepo.count();
    }

    public long countByStatus(AdoptionStatus status) throws ShelterException {
        return adoptionRepo.findByStatus(status).size();
    }

    public long countAdoptions(int clientId) throws ShelterException {
        return adoptionRepo.findJoinedByClient(clientId).size();
    }

    public String buildHistoryTrail(int clientId, int animalId) throws ShelterException {
        List<JoinedAdoption> filtered = adoptionRepo.findJoinedByClient(clientId).stream()
            .filter(j -> j.getAdoption().getAnimalId().equals(animalId))
            .collect(java.util.stream.Collectors.toList());

        if (filtered.isEmpty()) {
            return "(no prior adoption requests)";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filtered.size(); i++) {
            JoinedAdoption ja = filtered.get(i);
            sb.append("  Attempt #").append(i + 1).append(" on ").append(ja.getAdoption().getAdoptionDate())
                .append(" — ").append(ja.getAdoption().getStatus()).append("\n");
        }
        return sb.toString();
    }

    // PRIVATE HELPERS
    private Adoption requireExisting(int id) throws ShelterException {
        return adoptionRepo.findById(id).orElseThrow(() -> new AdoptionNotFoundException(id));
    }
}