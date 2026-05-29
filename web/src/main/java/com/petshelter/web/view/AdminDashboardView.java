package com.petshelter.web.view;

import com.petshelter.model.User;

import static com.petshelter.web.view.Html.*;

public final class AdminDashboardView {

    private AdminDashboardView() {}

    public static String render(User currentUser, long totalAnimals, long available, long adopted, long pendingAdoptions) {
        return Layout.page("Admin dashboard", currentUser,
            h1("Admin dashboard"),
            p("Welcome, " + currentUser.getDisplayName() + "."),
            tag("ul").with(
                tag("li").text("Total animals: " + totalAnimals),
                tag("li").text("Available: " + available),
                tag("li").text("Adopted: " + adopted),
                tag("li").text("Pending adoptions: " + pendingAdoptions)
            )
        );
    }
}