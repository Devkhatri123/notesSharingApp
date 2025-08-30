package com.notesSharingApp.notesSharingApp.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportedNoteDTO {
    private String noteID;
    private String noteName;
    private String subjectName;
    private int reportCount;
}
