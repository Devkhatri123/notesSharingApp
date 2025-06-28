package com.notesSharingApp.notesSharingApp.repository;

import com.notesSharingApp.notesSharingApp.DTO.notesDTO;
import com.notesSharingApp.notesSharingApp.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface notesRepo extends JpaRepository<Note, UUID> {
    public List<Note> findByisApproved(boolean isApproved);
}
