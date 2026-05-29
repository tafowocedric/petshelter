package com.petshelter.web.middleware;

import com.petshelter.model.Admin;
import com.petshelter.model.User;
import com.petshelter.web.controller.Controller;
import com.petshelter.web.http.Response;
import com.petshelter.web.session.CurrentUser;
import com.petshelter.web.session.SessionManager;
import com.petshelter.web.view.ErrorView;

public final class Guards {

    private Guards() {}

    public static Controller authenticated(SessionManager sessions, Controller next) {
        return req -> CurrentUser.from(req, sessions)
            .map(u -> safeHandle(next, req))
            .orElseGet(() -> new Response(req.exchange())
                .redirect("/login?error=Please+sign+in"));
    }

    public static Controller adminOnly(SessionManager sessions, Controller next) {
        return req -> {
            User user = CurrentUser.from(req, sessions).orElse(null);
            if (user == null) {
                return new Response(req.exchange()).redirect("/login?error=Please+sign+in");
            }
            if (!(user instanceof Admin)) {
                return new Response(req.exchange()).status(403).html(ErrorView.forbidden(user));
            }
            return next.handle(req);
        };
    }

    private static Response safeHandle(Controller c, com.petshelter.web.http.Request req) {
        try {
            return c.handle(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}