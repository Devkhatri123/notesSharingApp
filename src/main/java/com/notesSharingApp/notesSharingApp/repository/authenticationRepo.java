package com.notesSharingApp.notesSharingApp.repository;

import com.notesSharingApp.notesSharingApp.model.user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface authenticationRepo extends JpaRepository<user, UUID> {
    public user findByemail(String email);
    public user findByuniversityEmail(String rollNumber);
    public user findByContact(String phone);

}
