package com.notesSharingApp.notesSharingApp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;
    @Column(unique = true)
    private String resetToken;
    private LocalDateTime expiresAt;
    @OneToOne
    private User user;

    public boolean isExpired(){
      return expiresAt.isBefore(LocalDateTime.now());
    }
}
