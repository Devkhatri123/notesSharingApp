package com.notesSharingApp.notesSharingApp.DTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class ReportRequestDTO {
    private String reportID;
    private String reportedBy;
    private String reportedUser;
    private String reason;
    private String additionalDetails;
    private String reportedNote;
}
