package com.petshelter.web.view;

import com.petshelter.model.User;

import static com.petshelter.web.view.Html.*;

public final class BrowseView {

    private BrowseView() {}

    public static String render(User currentUser) {
        return Layout.page("Browse animals", currentUser,
            Layout.card(
                h1("Browse animals"),
                p("Hello, " + currentUser.getDisplayName() + ". The animal list is coming in the next phase.")
            )
        );
    }
}