package com.petshelter.web.view;

import com.petshelter.model.User;

import java.util.List;

import static com.petshelter.web.view.Html.*;

public final class UserListView {

    private UserListView() {}

    public static String render(User currentUser, List<User> users, String notice, String error) {
        Element tbody = tbody();
        for (User u : users) {
            tbody.with(
                tr().with(
                    td(String.valueOf(u.getId())),
                    td(u.getUsername()),
                    td(u.getFullName()),
                    td(u.getEmail()),
                    td(u.getPhone() == null ? "—" : u.getPhone()),
                    td(u.getRole().name()),
                    td("").with(actionsFor(currentUser, u))
                )
            );
        }

        return Layout.page("Пользователи", currentUser, notice, error,
            h1("Пользователи"),
            p("Все зарегистрированные пользователи. Нельзя удалить свой аккаунт или других администраторов."),
            table().with(
                thead().with(
                    tr().with(
                        th("ID"), th("Имя пользователя"), th("Полное имя"),
                        th("Email"), th("Телефон"), th("Роль"), th("Действия")
                    )
                ),
                tbody
            )
        );
    }

    private static Node actionsFor(User currentUser, User row) {
        boolean isSelf = currentUser.getId() != null && currentUser.getId().equals(row.getId());
        boolean isOtherAdmin = "ADMIN".equals(row.getRole().name()) && !isSelf;

        if (isSelf || isOtherAdmin) {
            return span().cls("muted").text("—");
        }

        return form().method("post").action("/admin/users/" + row.getId() + "/delete").with(button("Удалить")
        );
    }
}