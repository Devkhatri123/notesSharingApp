package com.notesSharingApp.notesSharingApp.Exception;

public class TokenExpired extends RuntimeException {
    public TokenExpired(String message) {
        super(message);
    }
}
