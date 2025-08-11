package com.notesSharingApp.notesSharingApp.model;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class UserReport {
    @Id
    private String reportID;
    @ManyToOne
    @JoinColumn(name = "reportedBy_UserId")
    private User reportedBy;
    @ManyToOne
    @JoinColumn(name = "reported_UserId",referencedColumnName = "id")
    private User reportedUser;
    private String reason;
    @Column(length = 512)
    private String additionalDetails;


}
