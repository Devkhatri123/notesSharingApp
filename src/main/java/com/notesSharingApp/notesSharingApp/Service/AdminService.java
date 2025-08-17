package com.notesSharingApp.notesSharingApp.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class AdminService {
    @Autowired
    private NotesService notesService;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private ReportService reportService;

    public HashMap<String,Long> getCounts(){
        HashMap<String,Long> countCategories = new HashMap<>();
        countCategories.put("pendingNotes",notesService.getPendingNoteCount() > 0 ? notesService.getPendingNoteCount() : 0);
        countCategories.put("pendingUpdates",profileService.getPendingUpdatesProfiles() > 0 ? profileService.getPendingUpdatesProfiles() : 0);
        countCategories.put("reportedUser",reportService.reportedUserCount() > 0 ? reportService.reportedUserCount() : 0);
        return countCategories;
    }
}
