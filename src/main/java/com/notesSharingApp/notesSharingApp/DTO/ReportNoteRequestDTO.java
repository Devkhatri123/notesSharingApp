package com.notesSharingApp.notesSharingApp.DTO;

import com.notesSharingApp.notesSharingApp.model.Note;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReportNoteRequestDTO {
    private String reportedBy;
    private String reportedNote;
    private String reason;
    private String additionalDetails;
}
