package com.deskpet.models;

import java.sql.SQLException;
import com.deskpet.data.DatabaseHandler;
import com.deskpet.domain.Pet;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;

public class DashboardModel {
    private final FloatProperty happiness = new SimpleFloatProperty(1);
    private final FloatProperty hunger = new SimpleFloatProperty(1);
    private final FloatProperty comfort = new SimpleFloatProperty(1);
    private final FloatProperty social = new SimpleFloatProperty(1);

    private DatabaseHandler db;

    public void setDbHandler(DatabaseHandler dbHandler) {
        db = dbHandler;
    }

    public float getHappiness() {
        return happiness.get();
    }
    // Updates the value of Happiness based on the four needs
    public void setHappiness() {
        //Calculate value of happiness
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

    public FloatProperty socialProperty() {
        return social;
    }

    public void updateState(Pet pet) throws SQLException {
        hunger.set(Math.clamp(pet.getHunger() / 100.0f, 0.0f, 1.0f));
        comfort.set(Math.clamp(pet.getComfort() / 100.0f, 0.0f, 1.0f));
        happiness.set(Math.clamp(pet.getHappiness() / 100.0f, 0.0f, 1.0f));
        social.set(Math.clamp(pet.getSocial() / 100.0f, 0.0f, 1.0f));

        db.updatePetState(pet);
    }
}