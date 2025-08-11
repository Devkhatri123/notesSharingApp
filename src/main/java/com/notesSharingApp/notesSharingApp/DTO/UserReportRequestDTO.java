package com.notesSharingApp.notesSharingApp.DTO;

import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
public class UserReportRequestDTO {
    private String reportID;
    private String reportedBy;
    private String reportedUser;
    private String reason;
    private String additionalDetails;
}
