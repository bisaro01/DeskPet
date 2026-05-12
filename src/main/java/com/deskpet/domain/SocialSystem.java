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
        if (noiseDecibels >= 30 && noiseDecibels <= 100) { // Nice sound level
            double increase = 1.0;
            setSocial(social + increase);
            lastSoundTime = System.currentTimeMillis();
            System.out.println("Sound level okay(" + noiseDecibels + "dB). Social +");
        }
        else if (noiseDecibels > 100) { // Too loud
            double decrease = ((noiseDecibels - 100) / 10.0) * 1.5;
            setSocial(social - decrease);
            lastSoundTime = System.currentTimeMillis();
            System.out.println("Environment is too loud! (" + noiseDecibels + "dB)! Social -");
        }
    }

    public void onMovement() {
        setSocial(social + 10);
        System.out.println("Movement detected");
    }

    public int applyDecay(double delta) {
        long currentTime = System.currentTimeMillis();
        double minutesSinceSound = (currentTime - lastSoundTime) / 60000.0;
        
        // Only apply decay if low sound detected for at least 30 seconds
        if (minutesSinceSound > 0.5) {
            double decay = 0.3 * (delta / 60.0);
            setSocial(social - decay);
        }

        if (social > 100) {social = 100;}
        if (social < 0) {social = 0;}

        return (int)social;
    }
}