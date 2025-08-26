package com.notesSharingApp.notesSharingApp.Exception.Account;

public class EmailAlreadyInUse extends RuntimeException {
    public EmailAlreadyInUse(String message) {
        super(message);
    }
}
