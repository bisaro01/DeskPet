package com.deskpet.domain;

import java.util.Set;

public class PetDisplayMapper {
    /* Encodes the things that affect the needs and happiness of the pet in a binary format.
    * 00000000 : no problems
    * 00000001 : pet is hungry
    * 00000010 : pet is hot (temperature too high)
    * 00000100 : pet is cold
    * 00001000 : pet is humid
    * 00010000 : pet is dry
    * 00100000 : pet is lonely
    * a |= b is same as a = a | b
    * 00000001 | 00000010 = 00000011 : pet is hungry and hot.
    * 00100000 | 00010000 = 00110000 : pet is dry and lonely.
      This fits in a single byte and should reduce MQTT network traffic. */
    private static final int HUNGRY_FLAG = 1 << 0;
    private static final int HOT_FLAG = 1 << 1;
    private static final int COLD_FLAG = 1 << 2;
    private static final int HUMID_FLAG = 1 << 3;
    private static final int DRY_FLAG = 1 << 4;
    private static final int LONELY_FLAG = 1 << 5;

    public PetDisplayState map(Pet pet) {
        byte moodId = encodeMood(pet.getMood());
        byte issues = encodeIssues(pet.getActiveIssues());

        PetDisplayState newState = new PetDisplayState(moodId, issues);
        return newState;
    }

    // Uses bitwise operations to indicate issues (what icons to show).
    private byte encodeIssues(Set<Need> issues) {
        byte flags = 0;

        if (issues.contains(Need.HUNGRY)) {
            flags |= HUNGRY_FLAG;
        }

        if (issues.contains(Need.HOT)) {
            flags |= HOT_FLAG;
        } else if (issues.contains(Need.COLD)) {
            flags |= COLD_FLAG;
        }

        if (issues.contains(Need.HUMID)) {
            flags |= HUMID_FLAG;
        } else if (issues.contains(Need.DRY)) {
            flags |= DRY_FLAG;
        }

        if (issues.contains(Need.LONELY)) {
            flags |= LONELY_FLAG;
        }

        return flags;
    }

    // Different faces are represented by an integer ID value where 0 is happy, 1 is sad. More can be added.
    private byte encodeMood(Mood mood) {
        switch(mood) {
            case VERY_SAD:
                return 0;

            case SAD:
                return 1;

            case NEUTRAL:
                return 2;

            case HAPPY:
                return 3;

            case VERY_HAPPY:
                return 4;

            default:
                return 2;
        }
    }
}
