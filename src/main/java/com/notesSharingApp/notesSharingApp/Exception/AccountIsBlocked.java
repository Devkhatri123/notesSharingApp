package com.notesSharingApp.notesSharingApp.Exception;

public class AccountIsBlocked extends RuntimeException {
    public AccountIsBlocked(String message) {
        super(message);
    }
}
