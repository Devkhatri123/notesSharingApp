package com.notesSharingApp.notesSharingApp.model;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;

import java.util.Objects;

@Entity
@Getter
@Setter
@EqualsAndHashCode
//@ToString(exclude = {"reportedUser","reportedBy"})
public class UserReport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String reportID;
    @ManyToOne
    @JoinColumn(name = "reportedBy_UserId")
    private User reportedBy;
    @ManyToOne
    @JoinColumn(name = "reported_UserId",referencedColumnName = "id")
    private User reportedUser;
    private String reason;
    @Column(length = 120)
    private String additionalDetails;


}
