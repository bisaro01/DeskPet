package com.deskpet.domain;

// Data packet to be published to the WIO terminal. See PetDisplayMapper for detailed explanation.
public class PetDisplayState {
    private final byte moodId;
    private final byte issueFlags;

    public PetDisplayState(byte mood, byte issues) {
        moodId = mood;
        issueFlags = issues;
    }

    public byte getMoodId() {
        return moodId;
    }

    public byte getIssueFlags() {
        return issueFlags;
    }

    public String toPayload() {
        return (moodId + "|" + issueFlags);
    }
}
