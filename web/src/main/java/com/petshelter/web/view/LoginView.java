package com.petshelter.web.view;

import com.petshelter.model.User;

import static com.petshelter.web.view.Html.*;

public final class LoginView {

    private LoginView() {}

    public static String render(User currentUser, String notice, String error, String username) {
        return Layout.page("Вход", currentUser, notice, error,
            Layout.narrowCard(
                h1("Войти"),
                form().method("post").action("/login").cls("form").with(
                    Layout.field("Имя пользователя", "username", "text", username, true),
                    Layout.field("Пароль", "password", "password", "", true),
                    button("Войти").cls("btn primary")
                ),
                p("Нет аккаунта? ").cls("muted").with(
                    a("/register", "Зарегистрироваться").cls("muted-link")
                )
            )
        );
    }
}