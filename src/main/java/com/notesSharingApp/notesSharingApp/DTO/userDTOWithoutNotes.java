package com.notesSharingApp.notesSharingApp.DTO;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class userDTOWithoutNotes {
    private String id;
    private String name;
    private String universityEmail;
    private int semester;
    private String department;
    private String role;
    private boolean isEnabled;
    private boolean isEmailVerified;

}
