package com.petshelter.web.view;

import com.petshelter.model.User;

import static com.petshelter.web.view.Html.*;

public final class AdminDashboardView {

    private AdminDashboardView() {}

    public static String render(User currentUser, long totalAnimals, long available, long adopted, long pendingAdoptions) {
        return Layout.page("Панель администратора", currentUser,
            h1("Панель администратора"),
            p("Добро пожаловать, " + currentUser.getDisplayName() + "."),
            tag("ul").with(
                tag("li").text("Всего животных: " + totalAnimals),
                tag("li").text("Доступно: " + available),
                tag("li").text("Усыновлено: " + adopted),
                tag("li").text("Ожидающие усыновления: " + pendingAdoptions)
            )
        );
    }
}