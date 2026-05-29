package com.petshelter.web.view;

import com.petshelter.model.User;

import static com.petshelter.web.view.Html.*;

public final class ErrorView {

    private ErrorView() {}

    public static String notFound(User currentUser, String path) {
        return Layout.page("Not found", currentUser,
            h1("404 — Not found"),
            p("The page you requested doesn't exist:"),
            p(path).cls("muted"),
            p("").with(a("/", "← Go home"))
        );
    }

    public static String forbidden(User currentUser) {
        return Layout.page("Forbidden", currentUser,
            h1("403 — Forbidden"),
            p("You don't have permission to view this page."),
            p("").with(a("/", "← Go home"))
        );
    }

    public static String serverError(User currentUser, String message) {
        return Layout.page("Server error", currentUser,
            h1("500 — Server error"),
            p("Something went wrong on our end."),
            p(message == null ? "" : message).cls("muted"),
            p("").with(a("/", "← Go home"))
        );
    }
}