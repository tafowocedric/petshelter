package com.petshelter.repository;

import com.petshelter.model.Adoption;
import com.petshelter.model.Animal;
import com.petshelter.model.User;

/**
 * Result type for join queries — an Adoption together with its fully-hydrated Animal and Client.
 */
public class JoinedAdoption {
    private final Adoption adoption;
    private final Animal animal;
    private final User client;

    public JoinedAdoption(Adoption adoption, Animal animal, User client) {
        this.adoption = adoption;
        this.animal = animal;
        this.client = client;
    }

    public Adoption getAdoption() { return adoption; }
    public Animal getAnimal()     { return animal; }
    public User getClient()       { return client; }

    @Override
    public String toString() {
        return String.format("Adoption #%s — %s for %s [%s]", adoption.getId(), animal.getName(),
            client.getDisplayName(), adoption.getStatus());
    }
}