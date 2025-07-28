package com.notesSharingApp.notesSharingApp.DTO;

import com.notesSharingApp.notesSharingApp.model.Subject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class NotesWithoutImagesDTO {
    private String id;
    private String title;
    private String description;
    private String createdAt;
    private String status;
    private String remarks;
    private Subject subject;
}
