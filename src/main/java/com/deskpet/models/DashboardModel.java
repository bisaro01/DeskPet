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

    // Method used for binding and observing changes to the property.
    public FloatProperty happinessProperty() {
        return happiness;
    }

    public FloatProperty hungerProperty() {
        return hunger;
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