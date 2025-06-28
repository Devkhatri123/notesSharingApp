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
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Component
@Entity
public class user {
    @Id
    private String id;
    private String fullname;
    private String gender;
    private String universityEmail;
    private String email;
    private int semester;
    private String department;
    private String contact;
    @JsonIgnore
    private String password;
    @Column(name = "isEnabled")
    private boolean isEnabled;
    private String role;
    private int verificationCode;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDateTime expirationAt;
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Note> myNotes;
}
