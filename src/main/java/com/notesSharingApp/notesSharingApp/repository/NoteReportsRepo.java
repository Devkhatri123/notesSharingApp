package com.notesSharingApp.notesSharingApp.repository;

import com.notesSharingApp.notesSharingApp.model.Note;
import com.notesSharingApp.notesSharingApp.model.NoteReport;
import com.notesSharingApp.notesSharingApp.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NoteReportsRepo extends JpaRepository<NoteReport,String> {
    NoteReport findByReportedByAndReportedNote(User reportedBy, Note reportedNote);
    int countNoteReportByReportedNoteId(String reportedNoteId);
    List<NoteReport> getAllByReportedNoteId(String reportedNoteId, Pageable pageable);

    void deleteByReportedNoteId(String reportedNoteId);
}
