package com.notesSharingApp.notesSharingApp.repository;

import com.notesSharingApp.notesSharingApp.model.user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface authenticationRepo extends JpaRepository<user, String> {
    public user findByemail(String email);
    public user findByContact(String phone);

}
