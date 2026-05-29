package com.petshelter.web.view;

import com.petshelter.enums.Gender;
import com.petshelter.enums.Species;
import com.petshelter.model.Animal;
import com.petshelter.model.Bird;
import com.petshelter.model.Cat;
import com.petshelter.model.Dog;
import com.petshelter.model.User;

import static com.petshelter.web.view.Html.*;

public final class AnimalFormView {

    private AnimalFormView() {}

    public static String render(User currentUser, Animal existing, String error) {
        boolean editing = existing != null && existing.getId() != null;
        String action = editing ? "/admin/animals/" + existing.getId() + "/edit" : "/admin/animals/new";

        return Layout.page(editing ? "Edit animal" : "Add animal", currentUser, null, error,
            h1(editing ? "Edit animal" : "Add animal"),
            form().method("post").action(action).with(
                Layout.field("Name", "name", "text", val(existing, Animal::getName), true),
                speciesSelect(existing),
                Layout.field("Breed", "breed", "text", val(existing, Animal::getBreed), false),
                Layout.field("Age", "age", "number", existing != null ? String.valueOf(existing.getAge()) : "", true),
                genderSelect(existing),
                Layout.field("Weight (kg)", "weight", "number",
                    existing != null && existing.getWeight() != null ? existing.getWeight().toString() : "",
                    false).with(raw("")),
                Layout.field("Color", "color", "text", val(existing, Animal::getColor), false),
                Layout.field("Description", "description", "text", val(existing, Animal::getDescription), false),
                Layout.field("Trained (dogs only — true/false)", "isTrained", "text",
                    existing instanceof Dog ? String.valueOf(((Dog) existing).isTrained()) : "", false),
                Layout.field("Indoor (cats only — true/false)", "isIndoor", "text",
                    existing instanceof Cat ? String.valueOf(((Cat) existing).isIndoor()) : "", false),
                Layout.field("Can fly (birds only — true/false)", "canFly", "text",
                    existing instanceof Bird ? String.valueOf(((Bird) existing).canFly()) : "", false),
                button(editing ? "Save changes" : "Create animal")
            ),
            p("").with(a("/admin/animals", "← Back to animals"))
        );
    }

    private static Element speciesSelect(Animal a) {
        Element s = select("species");
        for (Species sp : Species.values()) {
            Element opt = option(sp.name(), sp.name());
            if (a != null && a.getSpecies() == sp) opt.attr("selected", "selected");
            s.with(opt);
        }
        return label("Species").with(s);
    }

    private static Element genderSelect(Animal a) {
        Element s = select("gender");
        s.with(option("", "—"));
        for (Gender g : Gender.values()) {
            Element opt = option(g.name(), g.name());
            if (a != null && a.getGender() == g) opt.attr("selected", "selected");
            s.with(opt);
        }
        return label("Gender").with(s);
    }

    private static String val(Animal a, java.util.function.Function<Animal, String> getter) {
        if (a == null) return "";
        String v = getter.apply(a);
        return v == null ? "" : v;
    }
}