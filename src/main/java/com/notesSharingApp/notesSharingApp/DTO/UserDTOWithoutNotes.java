package com.notesSharingApp.notesSharingApp.DTO;


import com.notesSharingApp.notesSharingApp.Enum.Role;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class UserDTOWithoutNotes {
    private String id;
    private String username;
    private String universityEmail;
    private int semester;
    private String department;
    private String accountStatus;
    private String accountRemarks;
    private String gender;
    private List<Role> roles;
    private boolean isEnabled;
    private boolean isEmailVerified;


}
