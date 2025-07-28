package com.notesSharingApp.notesSharingApp.model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@EqualsAndHashCode
public class Subject implements Serializable {
    @Column(name="subject_id")
    private String subjectId;
    @Column(name = "subject_name",length = 100)
    private String subjectName;
    private int semester;
    @Column(name="subject_code",length = 20,unique = true)
    @Id
    private String code;
    @Column(name = "short_description")
    private String shortDescription;
    private String department;

    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "subject",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Note> notes;
}
