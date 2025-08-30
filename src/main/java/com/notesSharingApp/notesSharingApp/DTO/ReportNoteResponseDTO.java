package com.notesSharingApp.notesSharingApp.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportNoteResponseDTO {
    private String reportID;
    private String reason;
    private String additionalDetails;
    private String reportedByName;
}
