package com.petshelter.model;

import com.petshelter.enums.AdoptionStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

// Links a Client to an Animal.
public class Adoption {
    private Integer id;
    private Integer animalId;
    private Integer clientId;
    private LocalDate adoptionDate;
    private AdoptionStatus status;
    private String notes;
    private Integer approvedBy;       // admin user id

    public Adoption(Integer animalId, Integer clientId) {
        this.animalId = Objects.requireNonNull(animalId, "animalId cannot be null");
        this.clientId = Objects.requireNonNull(clientId, "clientId cannot be null");
        this.adoptionDate = LocalDate.now();
        this.status = AdoptionStatus.PENDING;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getAnimalId() { return animalId; }
    public void setAnimalId(Integer animalId) { this.animalId = animalId; }

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    public LocalDate getAdoptionDate() { return adoptionDate; }
    public void setAdoptionDate(LocalDate adoptionDate) { this.adoptionDate = adoptionDate; }

    public AdoptionStatus getStatus() { return status; }
    public void setStatus(AdoptionStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Integer getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Integer approvedBy) { this.approvedBy = approvedBy; }

    public Receipt buildReceipt(Animal animal, User client) {
        return new Receipt(this, animal, client);
    }

    public static class Receipt {
        private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy");

        private final Adoption adoption;
        private final Animal animal;
        private final User client;

        public Receipt(Adoption adoption, Animal animal, User client) {
            this.adoption = adoption;
            this.animal = animal;
            this.client = client;
        }

        public String format() {
            StringBuilder sb = new StringBuilder();
            sb.append("====================================\n");
            sb.append("       ADOPTION RECEIPT\n");
            sb.append("====================================\n");
            sb.append("Receipt #: ").append(adoption.getId() != null ? adoption.getId() : "PENDING").append("\n");
            sb.append("Date:      ").append(adoption.getAdoptionDate().format(DATE_FMT)).append("\n");
            sb.append("Status:    ").append(adoption.getStatus()).append("\n");
            sb.append("------------------------------------\n");
            sb.append("Adopter:   ").append(client.getDisplayName()).append("\n");
            sb.append("Email:     ").append(client.getEmail()).append("\n");
            sb.append("------------------------------------\n");
            sb.append("Animal:    ").append(animal.getInfo()).append("\n");
            sb.append("Sound:     ").append(animal.makeSound()).append("\n");
            sb.append("Care:      ").append(animal.getCareInstructions()).append("\n");
            sb.append("====================================\n");
            if (adoption.getNotes() != null && !adoption.getNotes().isEmpty()) {
                sb.append("Notes: ").append(adoption.getNotes()).append("\n");
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return format();
        }
    }
}