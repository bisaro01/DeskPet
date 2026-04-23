package com.deskpet.models;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;

public class dashboardModel {
    private final FloatProperty happiness = new SimpleFloatProperty(1);
    private final FloatProperty hunger = new SimpleFloatProperty(1);
    private final FloatProperty comfort = new SimpleFloatProperty(1);

    public float getHappiness() {
        return happiness.get();
    }
    // Updates the value of Happiness based on the four needs
    public void setHappiness() {
        //Calculate value of happiness TODO: Will need to add the two remaining needs and change to / 4
        float happinessValue = (hunger.get() + comfort.get()) / 2;
        happiness.set(Math.clamp(happinessValue, 0.0f, 1.0f));
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
}
