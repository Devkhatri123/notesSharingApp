package com.notesSharingApp.notesSharingApp.DTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RegisterDTO {
    private String username;
    private String universityEmail;
    private int semester;
    private String department;
    private String gender;
    private String password;
}
