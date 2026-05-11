package com.deskpet.domain;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class HungerSystem {
    // Values for calculating hunger
    private double hunger = 100.0;

    // How much hunger decreases per second (-0.4 per minute, 0.4/60.0)
    private static final double hunger_decrease = 1.0; // For demo purposes

    // How much hunger increases when valid RFID tag is read
    //private static final double hunger_increase = 40.0;


    // Scheduler to repeatedly decrease hunger over time
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // Demo RFID tags that represent valid food items
    private final Set<String> foodTags = Set.of(
            "9C:DA:6C:06",
            "2A:88:64:06");

    // Returns current hunger value
    public double getHunger() {
        return hunger;
    }

    public void feed(String rfidTag) {

        if (foodTags.contains(rfidTag)) {
            hunger += hunger_increase;
        }

        if (hunger > 100) {hunger = 100;}
        if (hunger < 0) {hunger = 0.0;}
    }

    public int applyDecay(double delta) {
        hunger -= hunger_decrease * delta;

        if (hunger > 100) {hunger = 100;}
        if (hunger < 0) {hunger = 0.0;}

        return (int)hunger;
    }
}