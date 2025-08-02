package com.notesSharingApp.notesSharingApp.model;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@EqualsAndHashCode
@Entity
@ToString
public class Note implements Serializable {
    @Id
    @Column(name = "note_id")
    private String id;
    private String title;
    @Column(length = 300)
    private String description;
    @Transient
    private String subjectCode;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(length = 768)
    private String remarks;
    private String createdAt;
    private String updatedAt;
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id")
    private user createdBy;
    @Column(name = "note_thumbnail",columnDefinition = "LONGBLOB")
    private byte[] imgThumbNail;
    @Column(columnDefinition = "LONGBLOB")
    private byte[] notePdfData;
    private String thumbnailFilename;
    private String pdfNoteFilename;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return Objects.equals(id, note.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
