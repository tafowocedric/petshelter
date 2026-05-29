package com.petshelter.web.view;

import com.petshelter.model.User;

import static com.petshelter.web.view.Html.*;

public final class ErrorView {

    private ErrorView() {}

    public static String notFound(User currentUser, String path) {
        return Layout.page("Не найдено", currentUser,
            h1("404 — Страница не найдена"),
            p("Запрошенная страница не существует:"),
            p(path).cls("muted"),
            p("").with(a("/", "На главную"))
        );
    }

    public static String forbidden(User currentUser) {
        return Layout.page("Доступ запрещён", currentUser,
            h1("403 — Доступ запрещён"),
            p("У вас нет прав для просмотра этой страницы."),
            p("").with(a("/", "На главную"))
        );
    }

    public static String serverError(User currentUser, String message) {
        return Layout.page("Ошибка сервера", currentUser,
            h1("500 — Ошибка сервера"),
            p("Произошла ошибка на сервере."),
            p(message == null ? "" : message).cls("muted"),
            p("").with(a("/", "На главную"))
        );
    }
}
