package com.petshelter.model;

import com.petshelter.enums.Gender;
import com.petshelter.enums.Species;

import java.math.BigDecimal;

public class Bird extends Animal {
    private boolean canFly;

    public Bird(String name, String breed, int age, Gender gender, BigDecimal weight, String color, String description, boolean canFly) {
        super(name, Species.BIRD, breed, age, gender, weight, color, description);
        this.canFly = canFly;
    }

    public boolean canFly() { return canFly; }
    public void setCanFly(boolean canFly) { this.canFly = canFly; }

    @Override
    public String makeSound() {
        return "Tweet! Tweet!";
    }

    @Override
    public String getCareInstructions() {
        return canFly ? "Provide a large cage and allow daily flight time." : "Provide ground space and gentle handling.";
    }

    @Override
    public String getInfo() {
        return super.getInfo() + (canFly ? " (can fly)" : " (flightless)");
    }
}