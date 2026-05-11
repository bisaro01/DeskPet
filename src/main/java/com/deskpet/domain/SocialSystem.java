package com.deskpet.domain;

public class SocialSystem {
    private double soundIncreasePer10Db = 1.5;
    private double decayRatePerMinute = 2.0;
    private double soundThreshold = 40.0;
    
    private double social = 100.0;
    private long lastSoundTime = System.currentTimeMillis();
    
    public SocialSystem() {
        social = 100.0;
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
    
    public double getSoundThreshold() {
        return soundThreshold;
    }
    
    public void setSoundThreshold(double value) {
        this.soundThreshold = value;
    }
    
    public double getSoundIncreasePer10Db() {
        return soundIncreasePer10Db;
    }
    
    public void setSoundIncreasePer10Db(double value) {
        this.soundIncreasePer10Db = value;
    }
    
    public double getDecayRatePerMinute() {
        return decayRatePerMinute;
    }
    
    public void setDecayRatePerMinute(double value) {
        this.decayRatePerMinute = value;
    }
    
    public void updateSocial(int noiseDecibels) {
    if (noiseDecibels > soundThreshold) {
        double increase = ((noiseDecibels - soundThreshold) / 10.0) * soundIncreasePer10Db;
        setSocial(social + increase);  
        lastSoundTime = System.currentTimeMillis();
        
        System.out.println("Sound detected.");
    }
}
    
    public int applyDecay(double delta) {
        long currentTime = System.currentTimeMillis();
        double minutesSinceSound = (currentTime - lastSoundTime) / 60000.0;
        
        // Start decaying after 30 seconds of no sound
        if (minutesSinceSound > 0.5) {
            double decay = decayRatePerMinute * (delta / 60.0);
            social -= decay;
        }
        
        return (int)social;
    }
}