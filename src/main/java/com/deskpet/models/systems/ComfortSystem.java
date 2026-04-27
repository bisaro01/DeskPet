package com.deskpet.models.systems;

public class ComfortSystem {
    /*
    Attributes that can be changed depending on user
    settings in version 2 with getters and setters
    */
    private static double MinTemp = 18.0;
    private static double MaxTemp = 24.0;
    private static double MinHumidity = 30.0;
    private static double MaxHumidity = 50.0;


    // Values for comfort calculations
    private static final double COMFORT_INCREASE = 0.5;
    private static final double COMFORT_DECREASE = 1.0;

    private double comfort = 100.0;


    // getters and setters
    public double getComfort() {
        return comfort;
    }
    public void setComfort(double value) {
        this.comfort = value;
    }

    public double getMinTemp() {
        return  MinTemp;
    }
    public void setMinTemp(double value) {
        this.MinTemp = value;
    }

    public double getMaxTemp() {
        return  MaxTemp;
    }
    public void setMaxTemp(double value) {
        this.MaxTemp = value;
    }

    public double getMinHumidity() {
        return  MinHumidity;
    }
    public void setMinHumidity(double value) {
        this.MinHumidity = value;
    }

    public double getMaxHumidity() {
        return  MaxHumidity;
    }
    public void setMaxHumidity(double value) {
        this.MaxHumidity = value;
    }


    
}
