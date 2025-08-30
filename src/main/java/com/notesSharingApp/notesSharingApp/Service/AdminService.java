package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.Exception.Account.NotLoggedIn;
import com.notesSharingApp.notesSharingApp.Util.util;
import com.notesSharingApp.notesSharingApp.model.*;
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
    private UserReportService userReportService;

    public HashMap<String,Long> getCounts(){
        HashMap<String,Long> countCategories = new HashMap<>();
        countCategories.put("pendingNotes",notesService.getPendingNoteCount() > 0 ? notesService.getPendingNoteCount() : 0);
        countCategories.put("pendingUpdates",profileService.getPendingUpdatesProfiles() > 0 ? profileService.getPendingUpdatesProfiles() : 0);
        countCategories.put("reportedUser", userReportService.reportedUserCount() > 0 ? userReportService.reportedUserCount() : 0);
        return countCategories;
    }
}
