package com.deskpet.domain;

import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import com.deskpet.data.DatabaseHandler;

public class HungerSystem {
    // Values for calculating hunger
    private double hunger = 100.0;
    private DatabaseHandler dbHandler;

    // How much hunger decreases per second (-0.4 per minute, 0.4/60.0)
    private static final double hunger_decrease = 1.0; // For demo purposes

    // Scheduler to repeatedly decrease hunger over time
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // Demo RFID tags that represent valid food items
    private final Set<String> foodTags = Set.of(
            "9C:DA:6C:06",
            "2A:88:64:06");

    public HungerSystem(int hunger, DatabaseHandler dbHandler) {
        this.hunger = (double)hunger;
        this.dbHandler = dbHandler;
    }

    // Returns current hunger value
    public double getHunger() {
        return hunger;
    }

    public void feed(String rfidTag, long petId) {

        if (foodTags.contains(rfidTag)) {
            try {
                double hunger_increase = dbHandler.getHungerGain(rfidTag);
                hunger += hunger_increase;
                long foodId = dbHandler.getFoodId(rfidTag);
                dbHandler.appendFeedingLog(petId, foodId);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (hunger > 100) {hunger = 100;}
        if (hunger < 0) {hunger = 0.0;}
    }

    public void applyDecay(double delta) {
        hunger -= hunger_decrease * delta;

        if (hunger > 100) {hunger = 100;}
        if (hunger < 0) {hunger = 0.0;}
    }
}