package com.deskpet.domain;

public class SocialSystem {
    private double soundIncreasePer10Db = 1.5;
    private double motionIncrease = 5.0;
    private double decayRatePerMinute = 2.0;
    private double soundThreshold = 40.0;
    
    private static final double SOCIAL_INCREASE = 1.0;
    private static final double SOCIAL_DECREASE = 5.0;
    
    private double social = 100.0;
}

    // Getters and setters
    public double getSocial() {
        return social;
    }

    public void setSocial(double value) {
        if (value > 100) {
            this.social = 100;
        } else if (value < 0) {
            this.social = 0;
        } else {
            this.social = value;
        }
    }

    