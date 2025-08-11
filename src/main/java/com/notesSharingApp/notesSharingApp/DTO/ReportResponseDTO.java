package com.notesSharingApp.notesSharingApp.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportResponseDTO {
    private String reportID;
    private String reason;
    private String additionalDetails;
    private String reportedByUserId;
    private String reportedByUserName;
    private String reportedByUserEmail;
}
