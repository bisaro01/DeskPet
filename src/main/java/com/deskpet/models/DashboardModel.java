package com.deskpet.models;

import com.deskpet.DatabaseHandler;
import com.deskpet.domain.ComfortListener;
import com.deskpet.domain.HappinessListener;
import com.deskpet.domain.HungerListener;

import javafx.application.Platform;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;

public class DashboardModel implements HappinessListener, ComfortListener, HungerListener {
    private final FloatProperty happiness = new SimpleFloatProperty(1);
    private final FloatProperty hunger = new SimpleFloatProperty(1);
    private final FloatProperty comfort = new SimpleFloatProperty(1);

    private DatabaseHandler db;

    public void setDbHandler(DatabaseHandler dbHandler) {
        db = dbHandler;
    }

    public float getHappiness() {
        return happiness.get();
    }
    // Updates the value of Happiness based on the four needs
    public void setHappiness() {
        //Calculate value of happiness TODO: Will need to add the two remaining needs and change to / 4
        //        float happinessValue = (hunger.get() + comfort.get()) / 2;
        //        happiness.set(Math.clamp(happinessValue, 0.0f, 1.0f));
    }

    // Method used for binding and observing changes to the property.
    // I.e., to make sure the GUI updates automatically if the value updates.
    public FloatProperty happinessProperty() {
        return happiness;
    }

    public float getHunger() {
        return hunger.get();
    }

    public void setHunger(float value) {
        hunger.set(Math.clamp(value, 0.0f, 1.0f));
        setHappiness();
    }

    public FloatProperty hungerProperty() {
        return hunger;
    }

    public float getComfort() {
        return comfort.get();
    }

    public void setComfort(float value) {
        comfort.set(Math.clamp(value, 0.0f, 1.0f));
        setHappiness();
    }

    public FloatProperty comfortProperty() {
        return comfort;
    }

    @Override
    public void updateHappiness(double value) {
        Platform.runLater(() -> {
            happiness.set(Math.clamp((float)(value / 100.0), 0.0f, 1.0f));
            System.out.println("model: " + happiness);
        });
    }

    @Override
    public void updateComfort(double value) {
        Platform.runLater(() -> {
            comfort.set(Math.clamp((float) (value / 100.0F), 0.0F, 1.0F));
            System.out.println("comfort: " + comfort);
        });
    }

    @Override
    public void updateHunger(double value) {
        Platform.runLater(() -> {
            hunger.set(Math.clamp((float)(value / 100.0F), 0.0f, 1.0f));
            System.out.println("hunger: " + hunger);
        });
    }

    public void updateState(Pet pet) throws SQLException {
        hunger.set(Math.clamp(pet.getHunger() / 100.0f, 0.0f, 1.0f));
        comfort.set(Math.clamp(pet.getComfort() / 100.0f, 0.0f, 1.0f));
        happiness.set(Math.clamp(pet.getHappiness() / 100.0f, 0.0f, 1.0f));

        db.updatePetState(pet);
    }
}