package com.notesSharingApp.notesSharingApp.Exception;

public class VerificationCodeNotMatched extends RuntimeException {
    public VerificationCodeNotMatched(String message) {
        super(message);
    }
}
