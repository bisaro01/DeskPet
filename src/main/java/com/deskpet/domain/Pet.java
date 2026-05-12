package com.deskpet.domain;

import com.deskpet.data.DatabaseHandler;

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

    public void applyDecay(double delta) {
        comfortSystem.applyDecay(delta);
        hungerSystem.applyDecay(delta);
        socialSystem.applyDecay(delta);
    }

// Getters
    @Override
    public long getPetId() {
        return petId;
    }

    @Override
    public int getHunger() {
        return (int)hungerSystem.getHunger();
    }

    // @Override
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
}