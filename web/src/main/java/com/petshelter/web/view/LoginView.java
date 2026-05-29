package com.petshelter.web.view;

import com.petshelter.model.User;

import static com.petshelter.web.view.Html.*;

public final class LoginView {

    private LoginView() {}

    public static String render(User currentUser, String notice, String error, String username) {
        return Layout.page("Sign in", currentUser, notice, error,
            Layout.narrowCard(
                h1("Sign in"),
                form().method("post").action("/login").cls("form").with(
                    Layout.field("Username", "username", "text", username, true),
                    Layout.field("Password", "password", "password", "", true),
                    button("Sign in").cls("btn primary")
                ),
                p("Don't have an account? ").cls("muted").with(
                    a("/register", "Create one").cls("muted-link")
                )
            )
        );
    }
}