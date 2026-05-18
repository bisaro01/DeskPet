package com.deskpet.domain;

public interface PetStateListener {
    void updateTemperature(float temperature);
    void updateHumidity(float humidity);
    void feed(String food);
    void registerNoise(int db);
    void registerMovement();
}