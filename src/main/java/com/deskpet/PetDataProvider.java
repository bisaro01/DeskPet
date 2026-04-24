package com.deskpet;

/* Assumes Class Pet exists.
Validation must occur externally.
Assumes Pet has following functions:
- getHunger(), returns int
- getSocial(), returns int
- getComfort(), returns int
- getHappiness(), returns int
- getPetId(), returns long
 */

public interface PetDataProvider {
    int getHunger();

    int getSocial();

    int getComfort();

    int getHappiness();

    long getPetId();
}
