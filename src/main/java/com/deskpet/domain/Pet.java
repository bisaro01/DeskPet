package com.deskpet.domain;

import com.deskpet.data.DatabaseHandler;

import java.util.EnumSet;
import java.util.Set;

public class Pet implements PetDataProvider, PetStateListener {
    private final ComfortSystem comfortSystem;
    private final HungerSystem hungerSystem;
    private final SocialSystem socialSystem;

    private long petId;

    public Pet(long petId, int hunger, int social, int comfort, DatabaseHandler dbHandler) {
        this.petId = petId;
        comfortSystem = new ComfortSystem(comfort);
        hungerSystem = new HungerSystem(hunger, dbHandler);
        socialSystem = new SocialSystem(social);
    }

    // Decay need values based on time passed, if pet outside comfortable bounds.
    public void applyDecay(double delta) {
        comfortSystem.applyDecay(delta);
        hungerSystem.applyDecay(delta);
        socialSystem.applyDecay(delta);
    }

    public double getTemperature() {
        return comfortSystem.getTemperature();
    }

    public double getHumidity() {
        return comfortSystem.getHumidity();
    }

    @Override
    public long getPetId() {
        return petId;
    }

    @Override
    public int getHunger() {
        return (int)hungerSystem.getHunger();
    }

    @Override
    public int getSocial() {
        return (int)socialSystem.getSocial();
    }

    @Override
    public int getComfort() {
        return (int)comfortSystem.getComfort();
    }

    @Override
    public int getHappiness() {
        return (getHunger() + getComfort() + getSocial()) / 3;
    }

    public boolean wasMotionDetected() {
        return socialSystem.isMovementActive();
    }

    public boolean wasNoiseDetected() {
        return socialSystem.wasNoiseDetected();
    }

    @Override
    public void updateTemperature(float temperature) {
        comfortSystem.updateTemperature(temperature);
    }

    @Override
    public void updateHumidity(float humidity) {
        comfortSystem.updateHumidity(humidity);
    }

    @Override
    public void feed(String food) {
        hungerSystem.feed(food, petId);
    }

    @Override
    public void registerNoise(int db) {
        socialSystem.updateSocial(db);
    }

    @Override
    public void registerMovement() {
        socialSystem.onMovement();
    }

    public void update(double delta) {
        applyDecay(delta);
    }

    public Mood getMood() {
        int happiness = getHappiness();

        if (happiness > 80) {
            return Mood.VERY_HAPPY;
        } else if (happiness > 60) {
            return Mood.HAPPY;
        } else if (happiness > 40) {
            return Mood.NEUTRAL;
        } else if (happiness > 20) {
            return Mood.SAD;
        } else {
            return Mood.VERY_SAD;
        }
    }

    public Set<Need> getActiveIssues() {
        Set<Need> issues = EnumSet.noneOf(Need.class);

        if (hungerSystem.isHungry()) {
            issues.add(Need.HUNGRY);
        }

        if (comfortSystem.isHot()) {
            issues.add(Need.HOT);
        } else if (comfortSystem.isCold()) {
            issues.add(Need.COLD);
        }

        if (comfortSystem.isHumid()) {
            issues.add(Need.HUMID);
        } else if (comfortSystem.isDry()) {
            issues.add(Need.DRY);
        }

         if (socialSystem.isLonely()) {
             issues.add(Need.LONELY);
         }

         return issues;
    }
}