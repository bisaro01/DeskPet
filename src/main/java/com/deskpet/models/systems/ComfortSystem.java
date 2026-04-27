package com.deskpet.models.systems;

public class ComfortSystem {
    /*
    Attributes that can be changed depending on user
    settings in version 2 with getters and setters
    */
    private static float MinTemp = 18.0F;
    private static float MaxTemp = 24.0F;
    private static float MinHumidity = 30.0F;
    private static float MaxHumidity = 50.0F;


    // Values for comfort calculations
    private static final float COMFORT_INCREASE = 0.5F;
    private static final float COMFORT_DECREASE = 1.0F;

    private float comfort = 100.0F;


    // getters and setters
    public float getComfort() {
        return comfort;
    }
    public void setComfort(float value) {
        this.comfort = value;
    }

    public float getMinTemp() {
        return  MinTemp;
    }
    public void setMinTemp(float value) {
        this.MinTemp = value;
    }

    public double getMaxTemp() {
        return  MaxTemp;
    }
    public void setMaxTemp(float value) {
        this.MaxTemp = value;
    }

    public float getMinHumidity() {
        return  MinHumidity;
    }
    public void setMinHumidity(float value) {
        this.MinHumidity = value;
    }

    public float getMaxHumidity() {
        return MaxHumidity;
    }
    public void setMaxHumidity(float value) {
        this.MaxHumidity = value;
    }


    // Method for calculating comfort levels
    public void updateComfort(float humidity, float temperature) {
        // Logic for humidity comfort calculation
        if (humidity >= MinHumidity && humidity <= MaxHumidity) {
            comfort += COMFORT_INCREASE;
        } else {
            comfort -= COMFORT_DECREASE;
        }

        // Logic for temperature comfort calculation
        if (temperature >= MinTemp && temperature <= MaxTemp) {
            comfort += COMFORT_INCREASE;
        } else {
            comfort -= COMFORT_DECREASE;
        }

        // stop comfort from going outside bounds 0-100
        if (comfort > 100) {comfort = 100;}
        if (comfort < 0) {comfort = 0;}
    }
}
