package com.notesSharingApp.notesSharingApp.Exception.Account;

public class UsernameAlreadyTaken extends RuntimeException {
  public UsernameAlreadyTaken(String message) {
    super(message);
  }
}
