package com.notesSharingApp.notesSharingApp.DTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.notesSharingApp.notesSharingApp.model.Subject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class notesDTO {
    private String id;
    private String title;
    private String description;
    private String createdAt;
    private userDTOWithoutNotes createdBy;
    private Subject subject;
    private byte[] thumbnail;
    private byte[] notes;
}
