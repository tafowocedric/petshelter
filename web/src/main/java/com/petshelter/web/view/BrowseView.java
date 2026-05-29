package com.petshelter.web.view;

import com.petshelter.enums.Species;
import com.petshelter.model.Animal;
import com.petshelter.model.User;

import java.util.List;

import static com.petshelter.web.view.Html.*;

public final class BrowseView {

    private BrowseView() {}

    public static String render(User currentUser, List<Animal> animals, String selectedSpecies, String notice, String error) {
        return Layout.page("Browse animals", currentUser, notice, error,
            h1("Browse animals"),
            p("These animals are looking for a home.").cls("muted"),
            filterBar(selectedSpecies),
            animals.isEmpty()? p("No animals match this filter.").cls("muted") : animalsGrid(animals)
        );
    }

    private static Element filterBar(String selected) {
        Element form = form().method("get").action("/browse");
        Element select = select("species");
        select.with(option("", "All species"));

        for (Species s : Species.values()) {
            Element opt = option(s.name(), s.name());

            if (s.name().equals(selected)) opt.attr("selected", "selected");
            select.with(opt);
        }
        return form.with(label("Filter by species").with(select), button("Filter"));
    }

    private static Element animalsGrid(List<Animal> animals) {
        Element tbody = tbody();
        for (Animal a : animals) {
            tbody.with(
                tr().with(
                    td(String.valueOf(a.getId())),
                    td(a.getName()),
                    td(a.getSpecies().name()),
                    td(a.getBreed() == null ? "—" : a.getBreed()),
                    td(String.valueOf(a.getAge())),
                    td(a.getGender() == null ? "—" : a.getGender().name()),
                    td("").with(
                        a("/animals/" + a.getId(), "View")
                    )
                )
            );
        }
        return table().with(
            thead().with(
                tr().with(
                    th("ID"), th("Name"), th("Species"), th("Breed"),
                    th("Age"), th("Gender"), th("")
                )
            ),
            tbody
        );
    }
}