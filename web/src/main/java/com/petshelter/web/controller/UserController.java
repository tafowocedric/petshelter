package com.petshelter.web.controller;

import com.petshelter.exception.ShelterException;
import com.petshelter.model.User;
import com.petshelter.service.UserService;
import com.petshelter.web.http.Request;
import com.petshelter.web.http.Response;
import com.petshelter.web.session.CurrentUser;
import com.petshelter.web.session.SessionManager;
import com.petshelter.web.view.UserListView;

public class UserController {
    private final UserService users;
    private final SessionManager sessions;

    public UserController(UserService users, SessionManager sessions) {
        this.users = users;
        this.sessions = sessions;
    }

    public Response list(Request req) throws ShelterException {
        User current = CurrentUser.from(req, sessions).orElseThrow();
        return new Response(req.exchange()).html(
            UserListView.render(current, users.getAll(current), req.query("notice"), req.query("error"))
        );
    }

    public Response delete(Request req) {
        User current = CurrentUser.from(req, sessions).orElseThrow();
        int id = Integer.parseInt(req.param("id"));
        try {
            users.delete(current, id);
            return new Response(req.exchange()).redirect("/admin/users?notice=" + java.net.URLEncoder.encode("Пользователь удалён", java.nio.charset.StandardCharsets.UTF_8));
        } catch (ShelterException e) {
            return new Response(req.exchange())
                .redirect("/admin/users?error=" + java.net.URLEncoder.encode(
                    e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}