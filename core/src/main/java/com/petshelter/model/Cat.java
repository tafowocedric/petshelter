package com.petshelter.model;

import com.petshelter.enums.Gender;
import com.petshelter.enums.Species;

import java.math.BigDecimal;

public class Cat extends Animal {
    private boolean isIndoor;

    public Cat(String name, String breed, int age, Gender gender, BigDecimal weight, String color, String description, boolean isIndoor) {
        super(name, Species.CAT, breed, age, gender, weight, color, description);
        this.isIndoor = isIndoor;
    }

    public boolean isIndoor() { return isIndoor; }
    public void setIndoor(boolean indoor) { this.isIndoor = indoor; }

    @Override
    public String makeSound() {
        return "Meow!";
    }

    @Override
    public String getCareInstructions() {
        return isIndoor ? "Indoor cat — provide litter box and scratching post." : "Outdoor cat — ensure vaccinations are up to date.";
    }

    @Override
    public String getInfo() {
        return super.getInfo() + (isIndoor ? " (indoor)" : " (outdoor)");
    }
}