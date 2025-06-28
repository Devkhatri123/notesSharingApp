package com.notesSharingApp.notesSharingApp.DTO;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class jsonResponse {
    private String message;
    private HttpStatusCode httpStatusCode;
}
