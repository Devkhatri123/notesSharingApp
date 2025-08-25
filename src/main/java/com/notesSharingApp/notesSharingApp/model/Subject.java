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
    @Column(name="subject_id",length = 36,updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private String subjectId;
    @Column(name = "subject_name",length = 30)
    private String subjectName;
    private int semester;
    @Column(name="subject_code",length = 20,unique = true)
    private String code;
    @Column(name = "short_description",length = 120)
    private String shortDescription;
    private String department;
    @Enumerated(EnumType.STRING)
    private Status status;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate createdAt;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate updatedAt;
    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "created_by_id",foreignKey = @ForeignKey(name = "fk_createdBy_user",
            foreignKeyDefinition = "FOREIGN KEY (created_by_id) REFERENCES user(id) ON DELETE CASCADE"))
    private User createdBy;
    @ManyToOne
    @JoinColumn(name = "updated_by_id")
    private User updatedBy;
    @OneToMany(mappedBy = "subject",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Note> notes;

    @Override
    public String toString() {
        return "Subject{" +
                "subjectName='" + subjectName + '\'' +
                ", semester=" + semester +
                ", code='" + code + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                '}';
    }
}
