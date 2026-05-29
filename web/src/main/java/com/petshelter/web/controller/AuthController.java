package com.petshelter.web.controller;

import com.petshelter.exception.ShelterException;
import com.petshelter.model.Admin;
import com.petshelter.model.User;
import com.petshelter.service.AuthService;
import com.petshelter.web.http.Request;
import com.petshelter.web.http.Response;
import com.petshelter.web.session.CurrentUser;
import com.petshelter.web.session.Session;
import com.petshelter.web.session.SessionManager;
import com.petshelter.web.view.LoginView;
import com.petshelter.web.view.RegisterView;

public class AuthController {
    private final AuthService auth;
    private final SessionManager sessions;

    public AuthController(AuthService auth, SessionManager sessions) {
        this.auth = auth;
        this.sessions = sessions;
    }

    public Response showLogin(Request req) {
        User current = CurrentUser.from(req, sessions).orElse(null);
        return new Response(req.exchange()).html(
            LoginView.render(current, req.query("notice"), req.query("error"), null)
        );
    }

    public Response doLogin(Request req) {
        String username = req.form("username");
        String password = req.form("password");
        try {
            User user = auth.login(username, password);
            Session session = sessions.create(user);
            String target = user instanceof Admin ? "/admin" : "/browse";

            return new Response(req.exchange()).setCookie(SessionManager.COOKIE_NAME, session.token())
                .redirect(target);
        } catch (ShelterException e) {
            User current = CurrentUser.from(req, sessions).orElse(null);
            return new Response(req.exchange()).status(400)
                .html(LoginView.render(current, null, e.getMessage(), username));
        }
    }

    public Response showRegister(Request req) {
        User current = CurrentUser.from(req, sessions).orElse(null);
        return new Response(req.exchange()).html(
            RegisterView.render(current, req.query("error"), null, null, null, null)
        );
    }

    public Response doRegister(Request req) {
        String username = req.form("username");
        String fullName = req.form("fullName");
        String email = req.form("email");
        String phone = req.form("phone");
        try {
            User created = auth.register(username, req.form("password"), fullName, email, phone);
            Session session = sessions.create(created);

            return new Response(req.exchange()).setCookie(SessionManager.COOKIE_NAME, session.token())
                .redirect("/browse");

        } catch (ShelterException e) {
            User current = CurrentUser.from(req, sessions).orElse(null);
            return new Response(req.exchange()).status(400)
                .html(RegisterView.render(current, e.getMessage(), username, fullName, email, phone));
        }
    }

    public Response logout(Request req) {
        String token = req.cookie(SessionManager.COOKIE_NAME);
        sessions.destroy(token);
        return new Response(req.exchange()).clearCookie(SessionManager.COOKIE_NAME)
            .redirect("/login?notice=Signed+out");
    }
}