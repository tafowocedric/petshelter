package com.petshelter.web.view;

import com.petshelter.model.Animal;
import com.petshelter.model.User;

import java.util.List;

import static com.petshelter.web.view.Html.*;

public final class AnimalListView {

    private AnimalListView() {}

    public static String render(User currentUser, List<Animal> animals, String notice, String error) {
        Element tbody = tbody();
        for (Animal a : animals) {
            tbody.with(
                tr().with(
                    td(String.valueOf(a.getId())),
                    td(a.getName()),
                    td(a.getSpecies().name()),
                    td(a.getBreed() == null ? "—" : a.getBreed()),
                    td(String.valueOf(a.getAge())),
                    td(a.getStatus().name()),
                    td("").with(
                        span().cls("actions").with(
                            a("/admin/animals/" + a.getId() + "/edit", "Редактировать"),
                            text(" "),
                            form().method("post").action("/admin/animals/" + a.getId() + "/delete").with(
                                button("Удалить")
                            )
                        )
                    )
                )
            );
        }

        return Layout.page("Животные", currentUser, notice, error,
            h1("Животные"),
            p("").with(a("/admin/animals/new", "+ Добавить животное")),
            table().with(
                thead().with(
                    tr().with(
                        th("ID"), th("Имя"), th("Вид"), th("Порода"),
                        th("Возраст"), th("Статус"), th("Действия")
                    )
                ),
                tbody
            )
        );
    }
}
