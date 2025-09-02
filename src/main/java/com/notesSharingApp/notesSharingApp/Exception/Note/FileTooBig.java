package com.notesSharingApp.notesSharingApp.Exception.Note;

public class FileTooBig extends RuntimeException {
    public FileTooBig(String message) {
        super(message);
    }
}
