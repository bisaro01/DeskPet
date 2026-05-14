package com.deskpet.domain;

public class SocialSystem {
    private double soundIncreasePer10Db = 1.5;
    private double decayRatePerMinute = 2.0;
    private double maxSoundThreshold = 100.0;
    private double minSoundThreshold = 30.0;

    private double social = 100.0;
    private long lastSoundTime = System.currentTimeMillis();

    // movement state for over time increase/decay
    private boolean movementActive = false;
    private long lastMovementTime = 0;
    private static final double MOVEMENT_TIMEOUT = 30000.0; // If no movement for 30s time out

    // Default Social increase over time
    private boolean noiseDetected = false;
    private static final double SOCIAL_INCREASE = 1.0;  // +1 when movement or sound within bounds
    private static final double SOCIAL_DECAY = 3.0;

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

    public double getMaxSoundThreshold() {
        return maxSoundThreshold;
    }

    public void setMaxSoundThreshold(double value) {
        this.maxSoundThreshold = value;
    }

    public double getMinSoundThreshold() {
        return minSoundThreshold;
    }

    public void setMinSoundThreshold(double value) {
        this.minSoundThreshold = value;
    }

    public double getSoundIncreasePer10Db() {
        return soundIncreasePer10Db;
    }

    public void setSoundIncreasePer10Db(double value) {
        this.soundIncreasePer10Db = value;
    }

    public long getLastSoundTime() {
        return lastSoundTime;
    }

    public double getDecayRatePerMinute() {
        return decayRatePerMinute;
    }

    public void setDecayRatePerMinute(double value) {
        this.decayRatePerMinute = value;
    }

    public boolean isMovementActive() {
        return movementActive;
    }

    public void onMovement() {
        lastMovementTime = System.currentTimeMillis();
        movementActive = true;
        System.out.println("Movement detected");
    }

    public boolean wasNoiseDetected() {
        boolean detected = noiseDetected;
        noiseDetected = false;
        return detected;
    }

    public void updateSocial(int noiseDecibels) {
        if (noiseDecibels >= minSoundThreshold && noiseDecibels <= maxSoundThreshold) { // Nice sound level
            double increase = 1.0;
            setSocial(social + increase);
            lastSoundTime = System.currentTimeMillis();
            noiseDetected = true;
            System.out.println("Sound level okay(" + noiseDecibels + "dB). Social +");
        } else if (noiseDecibels > maxSoundThreshold) { // Too loud
            double decrease = ((noiseDecibels - 100) / 10.0) * 1.5;
            setSocial(social - decrease);
            lastSoundTime = System.currentTimeMillis();
            noiseDetected = true;
            System.out.println("Environment is too loud! (" + noiseDecibels + "dB)! Social -");
        }
    }

    public int applyDecay(double delta) {
        long currentTime = System.currentTimeMillis();
        double minutesSinceSound = (currentTime - lastSoundTime) / 60000.0;

        // Check for movement and if it needs tim out/reset
        if (movementActive && (currentTime - lastMovementTime) > MOVEMENT_TIMEOUT) {
            movementActive = false;
        }

        if (movementActive) {
            social += (SOCIAL_INCREASE / 60) * delta;
        }

        // Only apply decay if low sound detected for at least 30 seconds
        if (minutesSinceSound > 0.5) {
            double decay = SOCIAL_DECAY * (delta / 60.0);
            setSocial(social - decay);
        }

        if (social > 100) {
            social = 100;
        }
        if (social < 0) {
            social = 0;
        }

        return (int) social;
    }
}