package com.petshelter.web.view;

import com.petshelter.model.User;

import static com.petshelter.web.view.Html.*;

public final class RegisterView {

    private RegisterView() {}

    public static String render(User currentUser, String error,
                                String username, String fullName, String email, String phone) {
        return Layout.page("Регистрация", currentUser, null, error,
            Layout.narrowCard(
                h1("Создать аккаунт"),
                form().method("post").action("/register").cls("form").with(
                    Layout.field("Имя пользователя", "username", "text", username, true),
                    Layout.field("Полное имя", "fullName", "text", fullName, true),
                    Layout.field("Email", "email", "email", email, true),
                    Layout.field("Телефон", "phone", "text", phone, false),
                    Layout.field("Пароль", "password", "password", "", true),
                    button("Создать аккаунт").cls("btn primary")
                ),
                p("Уже есть аккаунт? ").cls("muted").with(
                    a("/login", "Войти").cls("muted-link")
                )
            )
        );
    }
}