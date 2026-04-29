package com.deskpet.models;

import com.deskpet.domain.HappinessListener;
import com.deskpet.domain.HungerListener;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HungerSystem {
    // Value for hunger calculations
    private double hunger = 100.0;
    private static final double hunger_decrease = 0.4/60; // base decay -0.4 per minute
    private static final double hunger_increase = 40.0; // +40 hunger when fed
    private HungerListener hungerListener;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // For demo purposes :-P
    private final Set<String> foodTags = Set.of(
            "9C:DA:6C:06",
            "2A:88:64:06");

    // getters and setters
    public double getHunger() {
        return hunger;
    }

    public void updateHunger(String rfidTag) {
        if (foodTags.contains(rfidTag)) {
            hunger += hunger_increase;
        }

        if (hunger > 100) {hunger = 100;}
        if (hunger < 0) {hunger = 0.0;}

        hungerListener.updateHunger(hunger);
    }

    public void decreaseHunger() {
        hunger -= hunger_decrease;
        System.out.println("Hunger decrease: " + hunger);
        hungerListener.updateHunger(hunger);
    }

    public void setHungerListener(HungerListener hungerListener) {
        this.hungerListener = hungerListener;
        scheduler.scheduleAtFixedRate(() -> {
            decreaseHunger();
        },0,1, TimeUnit.SECONDS);
    }
}