package com.notesSharingApp.notesSharingApp.repository;

import com.notesSharingApp.notesSharingApp.model.TempUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserUpdateRequest extends JpaRepository<TempUser,String> {
}
