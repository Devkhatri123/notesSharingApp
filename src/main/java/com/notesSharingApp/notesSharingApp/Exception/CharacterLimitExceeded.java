package com.notesSharingApp.notesSharingApp.Exception;

public class CharacterLimitExceeded extends RuntimeException {
    public CharacterLimitExceeded(String message) {
        super(message);
    }
}
