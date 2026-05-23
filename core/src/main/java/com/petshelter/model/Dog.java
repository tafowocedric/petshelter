package com.petshelter.model;

import com.petshelter.enums.Gender;
import com.petshelter.enums.Species;

import java.math.BigDecimal;

public class Dog extends Animal {
    private boolean isTrained;

    public Dog(String name, String breed, int age, Gender gender, BigDecimal weight, String color, String description, boolean isTrained) {
        super(name, Species.DOG, breed, age, gender, weight, color, description);
        this.isTrained = isTrained;
    }

    public boolean isTrained() { return isTrained; }
    public void setTrained(boolean trained) { this.isTrained = trained; }

    // [POLYMORPHISM]
    @Override
    public String makeSound() {
        return "Woof! Woof!";
    }

    // [POLYMORPHISM]
    @Override
    public String getCareInstructions() {
        StringBuilder sb = new StringBuilder("Walk twice daily. Feed dog food 2x. ");
        sb.append(isTrained ? "Already trained — light reinforcement only." : "Needs basic obedience training.");
        return sb.toString();
    }

    @Override
    public String getInfo() {
        return super.getInfo() + (isTrained ? " (trained)" : " (untrained)");
    }
}