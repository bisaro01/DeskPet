package com.deskpet.domain;

public interface PetStateListener {
    void updateNeed(String topic, String value);
}