package com.petshelter.web.controller;

import com.petshelter.enums.Gender;
import com.petshelter.enums.Species;
import com.petshelter.exception.ShelterException;
import com.petshelter.model.Animal;
import com.petshelter.model.Bird;
import com.petshelter.model.Cat;
import com.petshelter.model.Dog;
import com.petshelter.model.User;
import com.petshelter.service.AnimalService;
import com.petshelter.web.http.Request;
import com.petshelter.web.http.Response;
import com.petshelter.web.session.CurrentUser;
import com.petshelter.web.session.SessionManager;
import com.petshelter.web.view.AnimalFormView;
import com.petshelter.web.view.AnimalListView;

import java.math.BigDecimal;

public class AnimalController {
    private final AnimalService animals;
    private final SessionManager sessions;

    public AnimalController(AnimalService animals, SessionManager sessions) {
        this.animals = animals;
        this.sessions = sessions;
    }

    public Response list(Request req) throws ShelterException {
        User current = CurrentUser.from(req, sessions).orElseThrow();
        return new Response(req.exchange()).html(
                AnimalListView.render(current, animals.getAll(), req.query("notice"), req.query("error"))
        );
    }

    public Response newForm(Request req) {
        User current = CurrentUser.from(req, sessions).orElseThrow();
        return new Response(req.exchange()).html(
                AnimalFormView.render(current, null, req.query("error"))
        );
    }

    public Response create(Request req) {
        User current = CurrentUser.from(req, sessions).orElseThrow();
        try {
            Animal animal = buildAnimalFromForm(req, null);
            animals.create(current, animal);
            return new Response(req.exchange()).redirect("/admin/animals?notice=Animal+created");
        } catch (Exception e) {
            return new Response(req.exchange()).status(400).html(
                AnimalFormView.render(current, null, e.getMessage())
            );
        }
    }

    public Response editForm(Request req) throws ShelterException {
        User current = CurrentUser.from(req, sessions).orElseThrow();
        int id = Integer.parseInt(req.param("id"));
        Animal existing = animals.getById(id);
        return new Response(req.exchange()).html(
            AnimalFormView.render(current, existing, req.query("error"))
        );
    }

    public Response update(Request req) throws ShelterException {
        User current = CurrentUser.from(req, sessions).orElseThrow();
        int id = Integer.parseInt(req.param("id"));

        try {
            Animal existing = animals.getById(id);
            Animal updated = buildAnimalFromForm(req, existing);
            updated.setId(id);
            updated.setStatus(existing.getStatus());
            updated.setArrivalDate(existing.getArrivalDate());
            animals.update(current, updated);
            return new Response(req.exchange()).redirect("/admin/animals?notice=Animal+updated");
        } catch (Exception e) {
            Animal existing = animals.getById(id);
            return new Response(req.exchange()).status(400).html(
                    AnimalFormView.render(current, existing, e.getMessage())
            );
        }
    }

    public Response delete(Request req) throws ShelterException {
        User current = CurrentUser.from(req, sessions).orElseThrow();
        int id = Integer.parseInt(req.param("id"));
        animals.delete(current, id);
        return new Response(req.exchange()).redirect("/admin/animals?notice=Animal+deleted");
    }

    private Animal buildAnimalFromForm(Request req, Animal existing) {
        String name = req.form("name");
        Species species = Species.valueOf(req.form("species"));
        String breed = nullIfBlank(req.form("breed"));
        int age = Integer.parseInt(req.form("age"));
        String genderStr = req.form("gender");
        Gender gender = (genderStr == null || genderStr.isEmpty()) ? null : Gender.valueOf(genderStr);
        BigDecimal weight = parseBigDecimal(req.form("weight"));
        String color = nullIfBlank(req.form("color"));
        String description = nullIfBlank(req.form("description"));

        switch (species) {
            case DOG:
                return new Dog(name, breed, age, gender, weight, color, description, parseBool(req.form("isTrained")));
            case CAT:
                return new Cat(name, breed, age, gender, weight, color, description, parseBool(req.form("isIndoor")));
            case BIRD:
                return new Bird(name, breed, age, gender, weight, color, description, parseBool(req.form("canFly")));
            default:
                throw new IllegalArgumentException("Unknown species");
        }
    }

    private static String nullIfBlank(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static BigDecimal parseBigDecimal(String s) {
        if (s == null || s.isBlank()) return null;
        try { return new BigDecimal(s); } catch (NumberFormatException e) { return null; }
    }

    private static boolean parseBool(String s) {
        return s != null && s.equalsIgnoreCase("true");
    }
}