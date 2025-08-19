package com.notesSharingApp.notesSharingApp.DTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ToString
public class loginDTO {
    private String email;
    private String password;

}
