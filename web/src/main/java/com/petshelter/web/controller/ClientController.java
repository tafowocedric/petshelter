package com.petshelter.web.controller;

import com.petshelter.enums.Species;
import com.petshelter.exception.ShelterException;
import com.petshelter.model.Animal;
import com.petshelter.model.User;
import com.petshelter.service.AdoptionService;
import com.petshelter.service.AnimalService;
import com.petshelter.web.http.Request;
import com.petshelter.web.http.Response;
import com.petshelter.web.session.CurrentUser;
import com.petshelter.web.session.SessionManager;
import com.petshelter.web.view.AnimalDetailView;
import com.petshelter.web.view.BrowseView;
import com.petshelter.web.view.MyAdoptionsView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ClientController {
    private final AnimalService animals;
    private final AdoptionService adoptions;
    private final SessionManager sessions;

    public ClientController(AnimalService animals, AdoptionService adoptions, SessionManager sessions) {
        this.animals = animals;
        this.adoptions = adoptions;
        this.sessions = sessions;
    }

    public Response browse(Request req) throws ShelterException {
        User current = CurrentUser.from(req, sessions).orElseThrow();
        String speciesFilter = req.query("species");

        List<Animal> available = animals.getAvailable();
        List<Animal> filtered = available;

        if (speciesFilter != null && !speciesFilter.isBlank()) {
            try {
                Species s = Species.valueOf(speciesFilter);
                filtered = animals.search(s).stream().filter(a -> a.getStatus().name().equals("AVAILABLE"))
                    .collect(java.util.stream.Collectors.toList());

            } catch (IllegalArgumentException e) {
                speciesFilter = null;
            }
        }

        return new Response(req.exchange()).html(BrowseView.render(current, filtered, speciesFilter,
            req.query("notice"), req.query("error"))
        );
    }

    public Response detail(Request req) throws ShelterException {
        User current = CurrentUser.from(req, sessions).orElseThrow();
        int id = Integer.parseInt(req.param("id"));
        Animal animal = animals.getById(id);
        return new Response(req.exchange()).html(AnimalDetailView.render(current, animal,
            req.query("notice"), req.query("error"))
        );
    }

    public Response requestAdoption(Request req) {
        User current = CurrentUser.from(req, sessions).orElseThrow();
        int animalId = Integer.parseInt(req.param("id"));
        String notes = req.form("notes");

        try {
            adoptions.request(current, animalId, notes);
            return new Response(req.exchange()).redirect("/my-adoptions?notice=Adoption+requested");
        } catch (ShelterException e) {
            String msg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return new Response(req.exchange()).redirect("/animals/" + animalId + "?error=" + msg);
        }
    }

    public Response myAdoptions(Request req) throws ShelterException {
        User current = CurrentUser.from(req, sessions).orElseThrow();
        return new Response(req.exchange()).html(
            MyAdoptionsView.render(current, adoptions.getMyAdoptions(current), req.query("notice"), req.query("error"))
        );
    }

    public Response cancelAdoption(Request req) {
        User current = CurrentUser.from(req, sessions).orElseThrow();
        int id = Integer.parseInt(req.param("id"));
        try {
            adoptions.cancel(current, id);
            return new Response(req.exchange()).redirect("/my-adoptions?notice=Adoption+cancelled");
        } catch (ShelterException e) {
            String msg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return new Response(req.exchange()).redirect("/my-adoptions?error=" + msg);
        }
    }
}