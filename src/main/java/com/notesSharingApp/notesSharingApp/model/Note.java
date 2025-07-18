package com.notesSharingApp.notesSharingApp.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@Entity
public class Note implements Serializable {
    @Id
    @Column(name = "note_id")
    private String id;
    private String title;
    private String description;
    @Transient
    private String subjectCode;
    private boolean isApproved;
    @Column(length = 512)
    private String remarks;
    private String createdAt;
    private String updatedAt;
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private user createdBy;
    @Column(name = "note_thumbnail",columnDefinition = "LONGBLOB")
    private byte[] imgThumbNail;
    @Column(columnDefinition = "LONGBLOB")
    private byte[] notePdfData;
}
