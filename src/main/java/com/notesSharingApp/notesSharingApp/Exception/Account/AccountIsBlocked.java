package com.notesSharingApp.notesSharingApp.Exception.Account;

public class AccountIsBlocked extends RuntimeException {
    public AccountIsBlocked(String message) {
        super(message);
    }
}
