package com.deskpet.models.systems;

public class ComfortSystem {
    /*
    Attributes that can be changed depending on user
    settings in version 2 with getters and setters
    */
    private static double min_temp = 18.0;
    private static double max_temp = 24.0;
    private static double min_humidity = 30.0;
    private static double max_humidity = 50.0;


    // Values for comfort calculations
    private static final double COMFORT_INCREASE = 0.5;
    private static final double COMFORT_DECREASE = 1.0;

    private double comfort = 100.0;
}
