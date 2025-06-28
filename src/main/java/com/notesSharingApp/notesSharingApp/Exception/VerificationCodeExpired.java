package com.notesSharingApp.notesSharingApp.Exception;

public class VerificationCodeExpired extends RuntimeException {
    public VerificationCodeExpired(String message) {
        super(message);
    }
}
