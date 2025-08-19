package com.notesSharingApp.notesSharingApp.Exception;

public class EmailAlreadyInUse extends RuntimeException {
    public EmailAlreadyInUse(String message) {
        super(message);
    }
}
