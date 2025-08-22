package com.notesSharingApp.notesSharingApp.DTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SubjectRequestDTO {
    private String subjectId;
    private String subjectName;
    private String shortDescription;
    private String code;
    private int semester;
}
