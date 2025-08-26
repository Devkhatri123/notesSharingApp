package com.notesSharingApp.notesSharingApp.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReportedUserDTO {
    private String id;
    private String username;
    private String universityEmail;
    private int semester;
    private String department;
    private String accountStatus;
    private String role;
    private boolean isEnabled;
    private Long reportCount;
    // private List<UserReportResponseDTO> reports;
}