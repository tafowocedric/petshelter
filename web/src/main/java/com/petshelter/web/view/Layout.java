package com.petshelter.web.view;

import com.petshelter.model.Admin;
import com.petshelter.model.User;

import static com.petshelter.web.view.Html.*;

public final class Layout {

    private Layout() {}

    public static String page(String title, User currentUser, String notice, String error, Node... body) {
        StringBuilder sb = new StringBuilder("<!DOCTYPE html>");
        tag("html").attr("lang", "en").with(
            tag("head").with(
                tag("meta").attr("charset", "utf-8").selfClosing(),
                tag("meta").attr("name", "viewport").attr("content", "width=device-width, initial-scale=1").selfClosing(),
                tag("title").text(title),
                tag("link").attr("rel", "stylesheet").attr("href", "/public/css/style.css").selfClosing()
            ),
            tag("body").with(
                topbar(currentUser),
                tag("main").cls("container").with(
                    notice != null ? alert("success", notice) : empty(),
                    error  != null ? alert("error", error)    : empty(),
                    each(java.util.Arrays.asList(body))
                )
            )
        ).render(sb);
        return sb.toString();
    }

    public static String page(String title, User currentUser, Node... body) {
        return page(title, currentUser, null, null, body);
    }

    private static Node topbar(User currentUser) {
        Element navEl = nav().cls("nav");
        if (currentUser == null) {
            navEl.with(
                a("/login", "Войти").cls("header-nav-item"),
                a("/register", "Регистрация").cls("header-nav-item")
            );
        } else {
            boolean isAdmin = currentUser instanceof Admin;
            if (isAdmin) {
                navEl.with(
                    a("/admin", "Панель управления").cls("header-nav-item"),
                    a("/admin/animals", "Животные").cls("header-nav-item"),
                    a("/admin/users", "Пользователи").cls("header-nav-item"),
                    a("/admin/adoptions", "Усыновления").cls("header-nav-item")
                );
            } else {
                navEl.with(
                    a("/browse", "Каталог").cls("header-nav-item"),
                    a("/my-adoptions", "Мои усыновления").cls("header-nav-item")
                );
            }
            navEl.with(
                span().cls("user").text(currentUser.getDisplayName()),
                form().method("post").action("/logout").cls("inline").with(
                    button("Выйти").cls("btn ghost small")
                )
            );
        }

        return tag("header").cls("topbar").with(
            a("/", "🐾 Приют").cls("brand"),
            navEl
        );
    }

    public static Element alert(String kind, String message) {
        return div().cls("alert " + kind).text(message);
    }

    public static Element card(Node... children) {
        return div().cls("card").with(children);
    }

    public static Element narrowCard(Node... children) {
        return div().cls("card narrow").with(children);
    }

    public static Element field(String labelText, String name, String type, String value, boolean required) {
        Element input = input().cls("input-form").type(type).name(name).value(value == null ? "" : value);
        if (required) input.required();
        return label(labelText).cls("form-label").with(input);
    }
}