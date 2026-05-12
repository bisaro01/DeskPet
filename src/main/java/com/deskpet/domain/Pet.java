package com.deskpet.domain;

public class Pet implements PetDataProvider, PetStateListener {
    private final ComfortSystem comfortSystem;
    private final HungerSystem hungerSystem;
    private final SocialSystem socialSystem;

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
        comfortSystem = new ComfortSystem();
        hungerSystem = new HungerSystem();
        socialSystem = new SocialSystem();
        calculateHappiness();
    }

    public void applyDecay(double delta) {
        comfort = comfortSystem.applyDecay(delta);
        hunger = hungerSystem.applyDecay(delta);
        social = socialSystem.applyDecay(delta);
    }


    public void calculateHappiness() {
        int happiness = (comfort + hunger + social) / 3;

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

    // @Override
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

    public boolean wasMotionDetected() {
        return socialSystem.isMovementActive();
    }

    public boolean wasNoiseDetected() {
        return socialSystem.wasNoiseDetected();
    }

    @Override
    public void updateNeed(String topic, String value) {
        switch (topic) {
            case "wio/sensors/temperature":
                float temp = Float.parseFloat(value);
                comfortSystem.updateTemperature(temp);
                break;

            case "wio/sensors/humidity":
                float hum = Float.parseFloat(value);
                comfortSystem.updateHumidity(hum);
                break;

            case "wio/events/food":
                hungerSystem.feed(value);
                hunger = (int)hungerSystem.getHunger();
                break;

            case "wio/sensors/sound":  
                int soundDecibels = Integer.parseInt(value);  
                socialSystem.updateSocial(soundDecibels);
                social = (int)socialSystem.getSocial();
                break;

            case "wio/sensors/motion":
                if (value.equals("1")) {
                    socialSystem.onMovement();
                    social = (int)socialSystem.getSocial();
                }
                break;
            
            default:
                break;
        }
        calculateHappiness();
    }

    public void update(double delta) {
        applyDecay(delta);
        calculateHappiness();
    }
}