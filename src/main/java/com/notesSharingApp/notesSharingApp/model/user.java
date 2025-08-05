package com.notesSharingApp.notesSharingApp.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import lombok.*;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Component
@Entity
public class user implements Serializable {
    @Id
    private String id;
    @Column(length = 50)
    private String fullname;
    private String gender;
    @Column(length = 20,unique = true)
    private String universityEmail;
    private int semester;
    private String department;
    private String contact;
    private String password;
    @Column(name = "isEnabled")
    private boolean isEnabled;
    @Column(name = "isEmailVerified")
    private boolean isEmailVerified;
    private String role;
    private int verificationCode;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDateTime expirationAt;
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Note> myNotes;
}
