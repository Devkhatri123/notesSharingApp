package com.notesSharingApp.notesSharingApp.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.notesSharingApp.notesSharingApp.Enum.AccountStatus;
import com.notesSharingApp.notesSharingApp.Enum.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
//@ToString
@Component
@Entity
public class User implements Serializable {
    @Id
    private String id;
    @Column(length = 35,unique = true)
    private String username;
    @Column(length = 7)
    private String gender;
    @Column(length = 20,unique = true)
    private String universityEmail;
    private int semester;
    @Column(length = 2)
    private String department;
    @JsonIgnore
    private String password;
    @Column(name = "isEnabled")
    private boolean isEnabled;
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;
    @Column(length = 512)
    private String accountRemarks;
    @Column(name = "isEmailVerified")
    private boolean isEmailVerified;
    private int verificationCode;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDateTime expirationAt;
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Note> myNotes;
    @OneToMany(mappedBy = "reportedUser",cascade = {CascadeType.PERSIST,CascadeType.REMOVE},fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UserReport> reports;
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    Set<Role> roles;


}
