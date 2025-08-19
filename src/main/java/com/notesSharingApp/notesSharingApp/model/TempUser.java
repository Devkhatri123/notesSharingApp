package com.notesSharingApp.notesSharingApp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Component
@Entity
public class TempUser {
    @Id
    private String id;
    @Column(length = 50)
    private String fullname;
    private String gender;
    @Column(length = 20)
    private String universityEmail;
    private int semester;
    private String department;
    private String contact;
    @Column(length = 512)
    private String remarks;
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate requestAt;

}
