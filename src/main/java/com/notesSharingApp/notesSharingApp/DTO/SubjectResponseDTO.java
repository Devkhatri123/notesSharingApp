package com.notesSharingApp.notesSharingApp.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class SubjectResponseDTO {
    private String subjectId;
    private String subjectName;
    private int semester;
    private String code;
    private String shortDescription;
    private String department;
    private String status;
    private String createdById;
    private String createdByName;
    private String editedById;
    private String editedByName;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate createdAt;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate updatedAt;
}
