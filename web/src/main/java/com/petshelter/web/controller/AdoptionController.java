package com.petshelter.web.controller;

import com.petshelter.enums.AdoptionStatus;
import com.petshelter.exception.ShelterException;
import com.petshelter.model.User;
import com.petshelter.repository.JoinedAdoption;
import com.petshelter.service.AdoptionService;
import com.petshelter.web.http.Request;
import com.petshelter.web.http.Response;
import com.petshelter.web.session.CurrentUser;
import com.petshelter.web.session.SessionManager;
import com.petshelter.web.view.AdoptionListView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AdoptionController {
    private final AdoptionService adoptions;
    private final SessionManager sessions;

    public AdoptionController(AdoptionService adoptions, SessionManager sessions) {
        this.adoptions = adoptions;
        this.sessions = sessions;
    }

    public Response list(Request req) throws ShelterException {
        User current = CurrentUser.from(req, sessions).orElseThrow();
        String statusFilter = req.query("status");

        List<JoinedAdoption> rows;
        if (statusFilter == null || statusFilter.isBlank()) {
            rows = adoptions.getAllJoined(current);
        } else {
            try {
                AdoptionStatus s = AdoptionStatus.valueOf(statusFilter);
                rows = filterJoined(adoptions.getAllJoined(current), s);
            } catch (IllegalArgumentException e) {
                rows = adoptions.getAllJoined(current);
                statusFilter = null;
            }
        }

        return new Response(req.exchange()).html(
            AdoptionListView.render(current, rows, statusFilter, req.query("notice"), req.query("error"))
        );
    }

    public Response approve(Request req) {
        return transition(req, "approve");
    }

    public Response reject(Request req) {
        return transition(req, "reject");
    }

    public Response complete(Request req) {
        return transition(req, "complete");
    }

    private Response transition(Request req, String action) {
        User current = CurrentUser.from(req, sessions).orElseThrow();
        int id = Integer.parseInt(req.param("id"));
        try {
            switch (action) {
                case "approve":  adoptions.approve(current, id); break;
                case "reject":   adoptions.reject(current, id, "Rejected by admin"); break;
                case "complete": adoptions.complete(current, id); break;
            }
            return new Response(req.exchange()).redirect("/admin/adoptions?notice=Adoption+" + action + "d");
        } catch (ShelterException e) {
            String msg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return new Response(req.exchange()).redirect("/admin/adoptions?error=" + msg);
        }
    }

    private static List<JoinedAdoption> filterJoined(List<JoinedAdoption> all, AdoptionStatus s) {
        return all.stream().filter(j -> j.getAdoption().getStatus() == s)
            .collect(java.util.stream.Collectors.toList());
    }
}