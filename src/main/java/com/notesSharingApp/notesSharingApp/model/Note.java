package com.notesSharingApp.notesSharingApp.model;


import com.notesSharingApp.notesSharingApp.Enum.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

import java.io.Serializable;

@Getter
@Setter
@ToString
@Entity
public class Note implements Serializable {
    @Id
    @Column(name = "note_id")
    private String id;
    @Column(length = 60)
    private String title;
    @Column(length = 300)
    private String description;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(length = 300)
    private String remarks;
    private String createdAt;
    private String updatedAt;
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User createdBy;
    @Column(name = "note_thumbnail",columnDefinition = "LONGBLOB")
    private String imgThumbNail;
    @Column(columnDefinition = "LONGBLOB")
    private String notePdfData;
    private String thumbnailFilename;
    private String pdfNoteFilename;
    @OneToMany(mappedBy = "reportedNote",cascade = {CascadeType.ALL})
    private Set<NoteReport> reports;
}
