package com.petshelter.web.view;

import com.petshelter.enums.AdoptionStatus;
import com.petshelter.model.User;
import com.petshelter.repository.JoinedAdoption;

import java.util.List;

import static com.petshelter.web.view.Html.*;

public final class AdoptionListView {

    private AdoptionListView() {}

    public static String render(User currentUser, List<JoinedAdoption> adoptions, String selectedStatus, String notice, String error) {
        return Layout.page("Adoptions", currentUser, notice, error,
            h1("Adoption requests"),
            filterBar(selectedStatus),
            adoptions.isEmpty() ? p("No adoptions match this filter.").cls("muted") : adoptionsTable(adoptions)
        );
    }

    private static Element filterBar(String selected) {
        Element form = form().method("get").action("/admin/adoptions");
        Element select = select("status");
        select.with(option("", "All statuses"));
        for (AdoptionStatus s : AdoptionStatus.values()) {
            Element opt = option(s.name(), s.name());

            if (s.name().equals(selected)) opt.attr("selected", "selected");
            select.with(opt);
        }
        return form.with(
            label("Filter by status").with(select),
            button("Filter")
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
                    td(j.getClient().getDisplayName()),
                    td(a.getStatus().name()),
                    td("").with(actionsFor(a.getId(), a.getStatus()))
                )
            );
        }
        return table().with(
            thead().with(
                tr().with(
                    th("ID"), th("Date"), th("Animal"), th("Client"),
                    th("Status"), th("Actions")
                )
            ),
            tbody
        );
    }

    private static Node actionsFor(Integer id, AdoptionStatus status) {
        switch (status) {
            case PENDING:
                return span().cls("actions").with(
                    form().method("post").action("/admin/adoptions/" + id + "/approve").with(button("Approve")),
                    text(" "),
                    form().method("post").action("/admin/adoptions/" + id + "/reject").with(button("Reject"))
                );
            case APPROVED:
                return form().method("post").action("/admin/adoptions/" + id + "/complete").with(button("Mark completed"));
            default:
                return span().cls("muted").text("—");
        }
    }
}