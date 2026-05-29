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
                tag("li").text("Species: " + animal.getSpecies()),
                tag("li").text("Breed: " + (animal.getBreed() == null ? "—" : animal.getBreed())),
                tag("li").text("Age: " + animal.getAge() + " years"),
                tag("li").text("Gender: " + (animal.getGender() == null ? "—" : animal.getGender())),
                tag("li").text("Weight: " + (animal.getWeight() == null ? "—" : animal.getWeight() + " kg")),
                tag("li").text("Color: " + (animal.getColor() == null ? "—" : animal.getColor())),
                tag("li").text("Status: " + animal.getStatus()),
                tag("li").text(speciesSpecific(animal))
            ),
            animal.getDescription() != null && !animal.getDescription().isBlank() ? p(animal.getDescription()) : empty(),
            p("").with(strong("Sound: ")).with(text(animal.makeSound())),
            p("").with(strong("Care: ")).with(text(animal.getCareInstructions())),
            adoptButton(currentUser, animal),
            p("").with(a("/browse", "Back to browse"))
        );
    }

    private static String speciesSpecific(Animal animal) {
        if (animal instanceof Dog)  return "Trained: " + ((Dog) animal).isTrained();
        if (animal instanceof Cat)  return "Indoor: " + ((Cat) animal).isIndoor();
        if (animal instanceof Bird) return "Can fly: " + ((Bird) animal).canFly();
        return "";
    }

    private static Node adoptButton(User currentUser, Animal animal) {
        if (!(currentUser instanceof Client)) {
            return empty();
        }
        if (animal.getStatus() != AnimalStatus.AVAILABLE) {
            return p("This animal is not currently available for adoption.").cls("muted");
        }
        return form().method("post").action("/animals/" + animal.getId() + "/adopt").with(
            label("Notes (optional)").with(
                textarea("notes").attr("rows", "3").attr("placeholder", "Why you'd be a good fit...")
            ),
            button("Request adoption")
        );
    }
}
