package com.deskpet.models;

import com.deskpet.domain.PetDataProvider;

public class Pet implements PetDataProvider {
    private long petId;
    private int hunger;
    private int social;
    private int comfort;
    private int happiness;

    public Pet(long petId, int hunger, int social, int comfort) {
        this.petId = petId;
        this.hunger = hunger;
        this.social = social;
        this.comfort = comfort;
        calculateHappiness();
    }

    public void calculateHappiness() {
        int happiness = (social + comfort - hunger) / 3;

        if (happiness > 100) {
            this.happiness = 100;
        } else if (happiness < 0) {
            this.happiness = 0;
        } else {
            this.happiness = happiness;
        }
    }

// Getters
    @Override
    public long getPetId() {
        return petId;
    }

    @Override
    public int getHunger() {
        return hunger;
    }

    @Override
    public int getSocial() {
        return social;
    }

    @Override
    public int getComfort() {
        return comfort;
    }

    @Override
    public int getHappiness() {
        return happiness;
    }

// Setters
    public void setHunger(int hunger) {
        if (hunger > 100) {
            this.hunger = 100;
        } else if (hunger < 0) {
            this.hunger = 0;
        } else {
            this.hunger = hunger;
        }
        calculateHappiness();
    }

    public void setSocial(int social) {
        if (social > 100) {
            this.social = 100;
        } else if (social < 0) {
            this.social = 0;
        } else {
            this.social = social;
        }
        calculateHappiness();
    }

    public void setComfort(int comfort) {
        if (comfort > 100) {
            this.comfort = 100;
        } else if (comfort < 0) {
            this.comfort = 0;
        } else {
            this.comfort = comfort;
        }
        calculateHappiness();
    }
}