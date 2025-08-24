package com.notesSharingApp.notesSharingApp.Exception.Account;

public class NotLoggedIn extends RuntimeException {
    public NotLoggedIn(String message) {
        super(message);
    }
}
