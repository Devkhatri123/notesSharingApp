package com.notesSharingApp.notesSharingApp.Exception;

public class AccountIsDisabled extends RuntimeException {
    public AccountIsDisabled(String message) {
        super(message);
    }
}
