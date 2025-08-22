package com.notesSharingApp.notesSharingApp.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadNoteDTO {
    private String id;
    private String title;
    private String description;
    private String subjectCode;
}
