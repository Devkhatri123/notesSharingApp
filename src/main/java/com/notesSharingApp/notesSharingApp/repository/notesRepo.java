package com.notesSharingApp.notesSharingApp.repository;

import com.notesSharingApp.notesSharingApp.DTO.notesDTO;
import com.notesSharingApp.notesSharingApp.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface notesRepo extends JpaRepository<Note, String> {
    public List<Note> findByisApproved(boolean isApproved);
    @Query("select n from Note n where n.subject.code=:code")
    public List<Note> findBySubjectID(@Param("code") String code);
}
