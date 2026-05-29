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

        return Layout.page(editing ? "Редактировать животное" : "Добавить животное", currentUser, null, error,
            h1(editing ? "Редактировать животное" : "Добавить животное"),
            form().method("post").action(action).with(
                Layout.field("Имя", "name", "text", val(existing, Animal::getName), true),
                speciesSelect(existing),
                Layout.field("Порода", "breed", "text", val(existing, Animal::getBreed), false),
                Layout.field("Возраст", "age", "number", existing != null ? String.valueOf(existing.getAge()) : "", true),
                genderSelect(existing),
                Layout.field("Вес (кг)", "weight", "number",
                    existing != null && existing.getWeight() != null ? existing.getWeight().toString() : "",
                    false).with(raw("")),
                Layout.field("Цвет", "color", "text", val(existing, Animal::getColor), false),
                Layout.field("Описание", "description", "text", val(existing, Animal::getDescription), false),
                Layout.field("Дрессирован (только собаки [true/false])", "isTrained", "text",
                    existing instanceof Dog ? String.valueOf(((Dog) existing).isTrained()) : "", false),
                Layout.field("Домашний (только кошки [true/false])", "isIndoor", "text",
                    existing instanceof Cat ? String.valueOf(((Cat) existing).isIndoor()) : "", false),
                Layout.field("Умеет летать (только птицы [true/false])", "canFly", "text",
                    existing instanceof Bird ? String.valueOf(((Bird) existing).canFly()) : "", false),
                button(editing ? "Сохранить" : "Создать животное")
            ),
            p("").with(a("/admin/animals", "Назад к животным"))
        );
    }

    private static Element speciesSelect(Animal a) {
        Element s = select("species");
        for (Species sp : Species.values()) {
            Element opt = option(sp.name(), sp.name());
            if (a != null && a.getSpecies() == sp) opt.attr("selected", "selected");
            s.with(opt);
        }
        return label("Вид").with(s);
    }

    private static Element genderSelect(Animal a) {
        Element s = select("gender");
        s.with(option("", "—"));
        for (Gender g : Gender.values()) {
            Element opt = option(g.name(), g.name());
            if (a != null && a.getGender() == g) opt.attr("selected", "selected");
            s.with(opt);
        }
        return label("Пол").with(s);
    }

    private static String val(Animal a, java.util.function.Function<Animal, String> getter) {
        if (a == null) return "";
        String v = getter.apply(a);
        return v == null ? "" : v;
    }
}
