package com.notesSharingApp.notesSharingApp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
public class NoteReport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String reportID;
    @ManyToOne
    @JoinColumn(name = "reportedBy_UserId",referencedColumnName = "id")
    private User reportedBy;
    @ManyToOne
    @JoinColumn(name = "reported_NoteId")
    private Note reportedNote;
    private String reason;
    @Column(length = 120)
    private String additionalDetails;

    @Override
    public int hashCode(){
        return Objects.hash(reportedBy.getId(),reportedNote.getId());
    }
    @Override
    public boolean equals(Object o){
        if(o == null || o.getClass() != getClass()) return false;
        NoteReport noteReport = (NoteReport) o;
        return this.reportedBy.getId().equals(noteReport.getReportedBy().getId())
                &&
                this.reportedNote.getId().equals(noteReport.getReportedNote().getId());
    }
}
