package com.deskpet.domain;

import com.deskpet.data.DatabaseHandler;

import java.util.EnumSet;
import java.util.Set;

public class Pet implements PetDataProvider, PetStateListener {
    private final ComfortSystem comfortSystem;
    private final HungerSystem hungerSystem;
    private final SocialSystem socialSystem;

    private long petId;
    private String name;
    private String favouriteFood;
    private double happiness;
    // TODO get happiness from DB, update getHappiness()

    public Pet(long petId, String name, String favouriteFood, int happiness, int hunger, int social, int comfort, DatabaseHandler dbHandler) {
        this.petId = petId;
        this.name = name;
        this.favouriteFood = favouriteFood;
        this.happiness = happiness;
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

    public String getName() { return name; }

    public String getFavouriteFood() { return favouriteFood; }

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
        return (int)Math.round(happiness);
    }

    public double getMinTemp() {
        return comfortSystem.getMinTemp();
    }

    public double getMaxTemp() {
        return comfortSystem.getMaxTemp();
    }

    public double getMinHumidity() {
        return comfortSystem.getMinHumidity();
    }

    public double getMaxHumidity() {
        return comfortSystem.getMaxHumidity();
    }

    public double getMinNoise() {
        return socialSystem.getMinSoundThreshold();
    }

    public double getMaxNoise() {
        return socialSystem.getMaxSoundThreshold();
    }

    public boolean wasMotionDetected() {
        return socialSystem.isMovementActive();
    }

    public boolean wasNoiseDetected() {
        return socialSystem.wasNoiseDetected();
    }

    public void setTemperatureSettings(int minTemp, int maxTemp) {
        comfortSystem.setMinTemp(minTemp);
        comfortSystem.setMaxTemp(maxTemp);
    }

    public void setHumiditySettings(int minHumidity, int maxHumidity) {
        comfortSystem.setMinHumidity(minHumidity);
        comfortSystem.setMaxHumidity(maxHumidity);
    }

    public void setNoiseSettings(int minNoise, int maxNoise) {
        socialSystem.setMinSoundThreshold(minNoise);
        socialSystem.setMaxSoundThreshold(maxNoise);
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
        updateHappiness(delta);
    }

    private void updateHappiness(double delta) {
        double moodDelta = 0;

        moodDelta += hungerMoodEffect();
        moodDelta += comfortMoodEffect();
        moodDelta += socialMoodEffect();

        if (moodDelta > 0) {
            moodDelta *= 0.5;
        }

        happiness += moodDelta * delta;

        if (happiness > 100) happiness = 100;
        if (happiness < 0) happiness = 0;
    }

    private double socialMoodEffect() {
        double social = getSocial();

        // Fulfilled
        if (social > 80) {
            return +0.10;
        }

        // Good
        if (social > 60) {
            return +0.02;
        }

        // Okay
        if (social > 40) {
            return 0;
        }

        // Lonely
        if (social > 20) {
            return -0.09;
        }

        // Depressed
        return -0.27;
    }

    private double comfortMoodEffect() {
        double comfort = getComfort();

        // Very comfortable
        if (comfort > 80) {
            return +0.05;
        }

        // Comfortable
        if (comfort > 60) {
            return +0.02;
        }

        // Okay
        if (comfort > 40) {
            return 0;
        }

        // Uncomfortable
        if (comfort > 20) {
            return -0.05;
        }

        // Miserable
        return -0.20;
    }

    private double hungerMoodEffect() {
        double hunger = getHunger();

        // Well-fed
        if (hunger > 80) {
            return +0.03;
        }

        // Peckish
        if (hunger > 60) {
            return +0.01;
        }

        // Hungry
        if (hunger > 40) {
            return -0.04;
        }

        // Very hungry
        if (hunger > 20) {
            return -0.12;
        }

        // Starving
        return -0.35;
    }

    public Mood getMood() {
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