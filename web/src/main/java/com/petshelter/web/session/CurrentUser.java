package com.petshelter.web.session;

import com.petshelter.model.Admin;
import com.petshelter.model.User;
import com.petshelter.web.http.Request;

import java.util.Optional;

public final class CurrentUser {
    private CurrentUser() {}

    public static Optional<User> from(Request req, SessionManager sessions) {
        String token = req.cookie(SessionManager.COOKIE_NAME);
        return sessions.get(token).map(Session::user);
    }

    public static boolean isAdmin(User u) {
        return u instanceof Admin;
    }
}