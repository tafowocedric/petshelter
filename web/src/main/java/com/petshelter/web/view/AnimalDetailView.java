package com.petshelter.web.view;

import com.petshelter.enums.AnimalStatus;
import com.petshelter.model.Animal;
import com.petshelter.model.Bird;
import com.petshelter.model.Cat;
import com.petshelter.model.Client;
import com.petshelter.model.Dog;
import com.petshelter.model.User;

import static com.petshelter.web.view.Html.*;

public final class AnimalDetailView {

    private AnimalDetailView() {}

    public static String render(User currentUser, Animal animal, String notice, String error) {
        return Layout.page(animal.getName(), currentUser, notice, error,
            h1(animal.getName()),
            tag("ul").with(
                tag("li").text("Вид: " + animal.getSpecies()),
                tag("li").text("Порода: " + (animal.getBreed() == null ? "—" : animal.getBreed())),
                tag("li").text("Возраст: " + animal.getAge() + " лет"),
                tag("li").text("Пол: " + (animal.getGender() == null ? "—" : animal.getGender())),
                tag("li").text("Вес: " + (animal.getWeight() == null ? "—" : animal.getWeight() + " кг")),
                tag("li").text("Цвет: " + (animal.getColor() == null ? "—" : animal.getColor())),
                tag("li").text("Статус: " + animal.getStatus()),
                tag("li").text(speciesSpecific(animal))
            ),
            animal.getDescription() != null && !animal.getDescription().isBlank() ? p(animal.getDescription()) : empty(),
            p("").with(strong("Звук: ")).with(text(animal.makeSound())),
            p("").with(strong("Уход: ")).with(text(animal.getCareInstructions())),
            adoptButton(currentUser, animal),
            p("").with(a("/browse", "Назад к каталогу"))
        );
    }

    private static String speciesSpecific(Animal animal) {
        if (animal instanceof Dog)  return "Дрессирован: " + ((Dog) animal).isTrained();
        if (animal instanceof Cat)  return "Домашний: " + ((Cat) animal).isIndoor();
        if (animal instanceof Bird) return "Умеет летать: " + ((Bird) animal).canFly();
        return "";
    }

    private static Node adoptButton(User currentUser, Animal animal) {
        if (!(currentUser instanceof Client)) {
            return empty();
        }
        if (animal.getStatus() != AnimalStatus.AVAILABLE) {
            return p("Это животное сейчас недоступно для усыновления.").cls("muted");
        }
        return form().method("post").action("/animals/" + animal.getId() + "/adopt").with(
            label("Примечания (необязательно)").cls("form-label").with(
                textarea("notes").id("client-note").attr("rows", "5")
                    .attr("style", "width: 450px; height: 120px")
                    .attr("placeholder", "Почему вы подходите...")
            ),
            button("Запросить усыновление")
        );
    }
}
