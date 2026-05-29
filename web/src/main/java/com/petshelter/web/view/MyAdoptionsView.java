package com.petshelter.web.view;

import com.petshelter.enums.AdoptionStatus;
import com.petshelter.model.User;
import com.petshelter.repository.JoinedAdoption;

import java.util.List;

import static com.petshelter.web.view.Html.*;

public final class MyAdoptionsView {

    private MyAdoptionsView() {}

    public static String render(User currentUser, List<JoinedAdoption> adoptions,
                                String notice, String error) {
        return Layout.page("Мои усыновления", currentUser, notice, error,
            h1("Мои усыновления"),
            adoptions.isEmpty()
                ? p("Вы ещё не подавали заявку на усыновление. ").cls("muted").with(
                a("/browse", "Каталог животных")
            ) : adoptionsTable(adoptions)
        );
    }

    private static Element adoptionsTable(List<JoinedAdoption> adoptions) {
        Element tbody = tbody();
        for (JoinedAdoption j : adoptions) {
            var a = j.getAdoption();
            tbody.with(
                tr().with(
                    td(String.valueOf(a.getId())),
                    td(String.valueOf(a.getAdoptionDate())),
                    td(j.getAnimal().getName() + " (" + j.getAnimal().getSpecies() + ")"),
                    td(a.getStatus().name()),
                    td(a.getNotes() == null ? "—" : a.getNotes()),
                    td("").with(actionsFor(a.getId(), a.getStatus()))
                )
            );
        }
        return table().with(
            thead().with(
                tr().with(
                    th("ID"), th("Дата"), th("Животное"),
                    th("Статус"), th("Примечания"), th("Действия")
                )
            ),
            tbody
        );
    }

    private static Node actionsFor(Integer id, AdoptionStatus status) {
        if (status == AdoptionStatus.PENDING || status == AdoptionStatus.APPROVED) {
            return form().method("post").action("/my-adoptions/" + id + "/cancel").with(button("Отменить"));
        }
        return span().cls("muted").text("—");
    }
}