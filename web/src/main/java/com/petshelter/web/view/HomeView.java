package com.petshelter.web.view;

import com.petshelter.model.User;

import static com.petshelter.web.view.Html.*;

public final class HomeView {

    private HomeView() {}

    public static String render(User currentUser) {
        return Layout.page("Pet Shelter", currentUser,
            section().cls("hero").with(
                h1("Welcome to the Pet Shelter Management System!"),
                p("A simple application for managing shelter animals and adoptions.").cls("lead"),
                currentUser == null
                    ? div().cls("actions").with(
                    a("/login", "Sign in").cls("btn primary"),
                    a("/register", "Create an account").cls("btn ghost")
                ) : empty()
            )
        );
    }
}