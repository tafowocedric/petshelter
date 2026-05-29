package com.petshelter.model;

import com.petshelter.enums.AnimalStatus;
import com.petshelter.enums.Gender;
import com.petshelter.enums.Species;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

// Base class for all shelter animals.
public abstract class Animal {
    private Integer id;
    private String name;
    private final Species species;
    private String breed;
    private int age;
    private Gender gender;
    private BigDecimal weight;
    private String color;
    private String description;
    private AnimalStatus status;
    private LocalDate arrivalDate;

    protected Animal(String name, Species species, String breed, int age, Gender gender, BigDecimal weight, String color, String description) {
        setName(name);

        this.species = Objects.requireNonNull(species, "Вид не может быть null");
        this.breed = breed;

        setAge(age);
        this.gender = gender;
        this.weight = weight;
        this.color = color;
        this.description = description;
        this.status = AnimalStatus.AVAILABLE;
        this.arrivalDate = LocalDate.now();
    }


    public abstract String makeSound();
    public abstract String getCareInstructions();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя животного не может быть пустым");
        }
        this.name = name.trim();
    }

    public Species getSpecies() { return species; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public int getAge() { return age; }
    public void setAge(int age) {
        if (age < 0) {
            throw new IllegalArgumentException("Возраст не может быть отрицательным");
        }
        this.age = age;
    }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) {
        if (weight != null && weight.signum() < 0) {
            throw new IllegalArgumentException("Weight cannot be negative");
        }
        this.weight = weight;
    }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public AnimalStatus getStatus() { return status; }
    public void setStatus(AnimalStatus status) {
        this.status = Objects.requireNonNull(status, "Статус не может быть null");
    }

    public LocalDate getArrivalDate() { return arrivalDate; }
    public void setArrivalDate(LocalDate arrivalDate) { this.arrivalDate = arrivalDate; }


    public String getInfo() {
        return String.format("[%s] %s — %s, %d years old, %s",
                species, name, breed != null ? breed : "unknown breed", age, status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Animal)) return false;
        Animal other = (Animal) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getInfo();
    }
}