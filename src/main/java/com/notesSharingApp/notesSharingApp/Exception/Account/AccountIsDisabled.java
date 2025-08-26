package com.notesSharingApp.notesSharingApp.Exception.Account;

public class AccountIsDisabled extends RuntimeException {
    public AccountIsDisabled(String message) {
        super(message);
    }
}
