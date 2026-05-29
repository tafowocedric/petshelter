package com.petshelter.web.view;

import com.petshelter.model.User;

import static com.petshelter.web.view.Html.*;

public final class AdminDashboardView {

    private AdminDashboardView() {}

    public static String render(User currentUser) {
        return Layout.page("Admin dashboard", currentUser,
            Layout.card(
                h1("Admin dashboard"),
                p("Welcome, " + currentUser.getDisplayName() + ". Full admin features come next phase.")
            )
        );
    }
}