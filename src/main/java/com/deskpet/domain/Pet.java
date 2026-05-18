package com.deskpet.domain;

import java.sql.SQLException;

import com.deskpet.data.DatabaseHandler;

public class Pet implements PetDataProvider, PetStateListener {
    private final ComfortSystem comfortSystem;
    private final HungerSystem hungerSystem;
    private final SocialSystem socialSystem;
    private final DatabaseHandler dbHandler; 

    private long petId;
    private String name;
    private String favouriteFood;
    private int hunger;
    private int social;
    private int comfort;
    private int happiness;

    public Pet(long petId, String name, String favouriteFood, int hunger, int social, int comfort, DatabaseHandler dbHandler) {
        this.petId = petId;
        this.name = name;
        this.favouriteFood = favouriteFood;
        this.hunger = hunger;
        this.social = social;
        this.comfort = comfort;
        this.dbHandler = dbHandler;
        comfortSystem = new ComfortSystem();
        hungerSystem = new HungerSystem(dbHandler);
        socialSystem = new SocialSystem(dbHandler);

        // read and load previous states fom the DB
        comfortSystem.setComfort(comfort);
        hungerSystem.setHunger(hunger);
        socialSystem.setSocial(social);
        
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

    public String getName() { return name; }

    public String getFavouriteFood() { return favouriteFood; }

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

    // Methods to get the last temperature and humidity readings for the DB
    public double getTemperature() {
        return comfortSystem.getTemperature();
    }
    public double getHumidity() {
        return comfortSystem.getHumidity();
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
                hungerSystem.feed(value, petId);
                hunger = (int)hungerSystem.getHunger();
                break;

            case "wio/sensors/sound":  
                int soundDecibels = Integer.parseInt(value);  
                socialSystem.updateSocial(soundDecibels);
                social = (int)socialSystem.getSocial();
                
                try {
                    dbHandler.logSensorData(petId, "SOUND", soundDecibels);
                } catch (SQLException e) {
                    System.err.println("Failed to log sound sensor data: " + e.getMessage());
                }
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