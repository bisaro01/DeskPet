package com.deskpet.models;

import java.util.Set;

public class HungerSystem {
    // Value for hunger calculations
    private double hunger = 100.0;
    private static final double hunger_decrease = 0.4; // base decay -0.4 per minute
    private static final double hunger_increase = 40.0; // +40 hunger when fed

    // For demo purposes :-P
    private final Set<String> foodTags = Set.of(
            "9C:DA:6C:06",
            "2A:88:64:06");

    // getters and setters
    public double getHunger() {
        return hunger;
    }

    public void setHunger(double value) {
        if (value > 100) {
            this.hunger = 100.0;
        } else if (value < 0) {
            this.hunger = 0.0;
        } else {
            this.hunger = value;
        }
    }
    public void updateHunger(String rfidTag) {
        if (foodTags.contains(rfidTag)) {
            hunger += hunger_increase;
        } else {
            hunger -= hunger_decrease;
        }

        if (hunger > 100) {hunger = 100;}
        if (hunger < 0) {hunger = 0.0;}
    }
}