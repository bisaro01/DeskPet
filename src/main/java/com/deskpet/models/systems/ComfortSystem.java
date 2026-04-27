package com.deskpet.models.systems;

public class ComfortSystem {
    /*
    Attributes that can be changed depending on user
    settings in version 2 with getters and setters
    */
    private float minTemp = 18.0F;
    private float maxTemp = 24.0F;
    private float minHumidity = 30.0F;
    private float maxHumidity = 50.0F;


    // Values for comfort calculations
    private static final float COMFORT_INCREASE = 0.5F;
    private static final float COMFORT_DECREASE = 1.0F;

    private float comfort = 100.0F;


    // getters and setters
    public float getComfort() {
        return comfort;
    }
    public void setComfort(float value) {
        if (value > 100) {
            this.comfort = 100;
        } else if (value < 0) {
            this.comfort = 0;
        } else {
            this.comfort = value;
        }
    }

    public float getMinTemp() {
        return  minTemp;
    }
    public void setMinTemp(float value) {
        this.minTemp = value;
    }

    public float getMaxTemp() {
        return  maxTemp;
    }
    public void setMaxTemp(float value) {
        this.maxTemp = value;
    }

    public float getMinHumidity() {
        return  minHumidity;
    }
    public void setMinHumidity(float value) {
        this.minHumidity = value;
    }

    public float getMaxHumidity() {
        return maxHumidity;
    }
    public void setMaxHumidity(float value) {
        this.maxHumidity = value;
    }


    // Method for calculating comfort levels
    public void updateComfort(float humidity, float temperature) {
        // Logic for humidity comfort calculation
        if (humidity >= minHumidity && humidity <= maxHumidity) {
            comfort += COMFORT_INCREASE;
        } else {
            comfort -= COMFORT_DECREASE;
        }

        // Logic for temperature comfort calculation
        if (temperature >= minTemp && temperature <= maxTemp) {
            comfort += COMFORT_INCREASE;
        } else {
            comfort -= COMFORT_DECREASE;
        }

        // stop comfort from going outside bounds 0-100
        if (comfort > 100) {comfort = 100;}
        if (comfort < 0) {comfort = 0;}
    }
}
