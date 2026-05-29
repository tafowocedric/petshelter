package com.petshelter.web.view;

import com.petshelter.model.User;

import static com.petshelter.web.view.Html.*;

public final class HomeView {

    private HomeView() {}

    public static String render(User currentUser) {
        return Layout.page("Приют для животных", currentUser,
            section().cls("hero").with(
                h1("Добро пожаловать в систему управления приютом!"),
                p("Приложение для управления животными и усыновлениями.").cls("lead"),
                currentUser == null
                    ? div().cls("actions").with(
                    a("/login", "Войти").cls("btn primary"),
                    a("/register", "Создать аккаунт").cls("btn ghost")
                ) : empty()
            )
        );
    }
}