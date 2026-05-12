package com.deskpet.domain;

public class ComfortSystem {
    /*
    Attributes that can be changed depending on user
    settings in version 2 with getters and setters
    */
    private double minTemp = 18.0;
    private double maxTemp = 24.0;
    private double minHumidity = 30.0;
    private double maxHumidity = 50.0;
    private double temperature;
    private double humidity;


    // Values for comfort calculations
    private static final double COMFORT_INCREASE = 1.0;
    private static final double COMFORT_DECREASE = 5.0;

    private double comfort = 100.0;

    public ComfortSystem(double comfort) {
        this.comfort = comfort;
        temperature = 23.0;
        humidity = 40.0;
    }


    // getters and setters
    public double getComfort() {
        return comfort;
    }
    public void setComfort(double value) {
        if (value > 100) {
            this.comfort = 100;
        } else if (value < 0) {
            this.comfort = 0;
        } else {
            this.comfort = value;
        }
    }

    public double getMinTemp() {
        return  minTemp;
    }
    public void setMinTemp(double value) {
        this.minTemp = value;
    }

    public double getMaxTemp() {
        return  maxTemp;
    }
    public void setMaxTemp(double value) {
        this.maxTemp = value;
    }

    public double getMinHumidity() {
        return  minHumidity;
    }
    public void setMinHumidity(double value) {
        this.minHumidity = value;
    }

    public double getMaxHumidity() {
        return maxHumidity;
    }
    public void setMaxHumidity(double value) {
        this.maxHumidity = value;
    }


    public void updateTemperature(float currentTemp) {
        temperature = currentTemp;
    }

    public void updateHumidity(float currentHumidity) {
        humidity = currentHumidity;
    }

    // Method for calculating comfort levels and applying decay
    public int applyDecay(double delta) {
        if (temperature < minTemp) {
            comfort -= (COMFORT_DECREASE / 60.0) * (minTemp - temperature) * delta;
        } else if (temperature > maxTemp) {
            comfort -= (COMFORT_DECREASE / 60.0) * (temperature - maxTemp) * delta;
        } else {
            comfort += (COMFORT_INCREASE / 60.0) * delta;
        }

        if (humidity < minHumidity) {
            comfort -= (COMFORT_DECREASE / 60.0) * (minHumidity - humidity) * delta;
        } else if (humidity > maxHumidity) {
            comfort -= (COMFORT_DECREASE / 60.0) * (humidity - maxHumidity) * delta;
        } else {
            comfort += (COMFORT_INCREASE / 60.0) * delta;
        }

        if (comfort > 100) {comfort = 100;}
        if (comfort < 0) {comfort = 0;}

        System.out.println("Pet.applyDecay - Temperature: " + temperature + ", Humidity: " + humidity);

        return (int)comfort;
    }
}
