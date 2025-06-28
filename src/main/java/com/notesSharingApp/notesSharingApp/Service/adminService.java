package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.DTO.notesDTO;
import com.notesSharingApp.notesSharingApp.model.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.notesSharingApp.notesSharingApp.repository.notesRepo;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class adminService {
    @Autowired
    private notesRepo notesRepo;

    public List<notesDTO> getApprovalPendingNotes() {
        List<Note> allNotes = notesRepo.findByisApproved(false);
        return allNotes.stream().map(notesService::getNotesDTO).toList();
    }
}
