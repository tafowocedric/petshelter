package com.petshelter.web.view;

import com.petshelter.model.User;

import static com.petshelter.web.view.Html.*;

public final class RegisterView {

    private RegisterView() {}

    public static String render(User currentUser, String error,
                                String username, String fullName, String email, String phone) {
        return Layout.page("Create account", currentUser, null, error,
            Layout.narrowCard(
                h1("Create an account"),
                form().method("post").action("/register").cls("form").with(
                    Layout.field("Username", "username", "text", username, true),
                    Layout.field("Full name", "fullName", "text", fullName, true),
                    Layout.field("Email", "email", "email", email, true),
                    Layout.field("Phone", "phone", "text", phone, false),
                    Layout.field("Password", "password", "password", "", true),
                    button("Create account").cls("btn primary")
                ),
                p("Already have an account? ").cls("muted").with(
                    a("/login", "Sign in").cls("muted-link")
                )
            )
        );
    }
}