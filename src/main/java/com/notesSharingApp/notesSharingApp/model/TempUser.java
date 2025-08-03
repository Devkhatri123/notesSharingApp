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
    private String name;
    private String gender;
    @Column(length = 20,unique = true,insertable = false,updatable = false)
    private String universityEmail;
    private int semester;
    private String department;
    private String phone;
    private String remarks;
    @Enumerated(EnumType.STRING)
    private Status accountStatus;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate requestAt;

}
