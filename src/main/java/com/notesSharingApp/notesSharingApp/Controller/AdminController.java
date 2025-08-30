package com.notesSharingApp.notesSharingApp.Controller;

import com.notesSharingApp.notesSharingApp.DTO.SubjectRequestDTO;
import com.notesSharingApp.notesSharingApp.DTO.SubjectResponseDTO;
import com.notesSharingApp.notesSharingApp.Exception.Account.NotLoggedIn;
import com.notesSharingApp.notesSharingApp.Exception.CharacterLimitExceeded;
import com.notesSharingApp.notesSharingApp.Exception.NotAllowed;
import com.notesSharingApp.notesSharingApp.Exception.Subject.SubjectAlreadyExists;
import com.notesSharingApp.notesSharingApp.Exception.Subject.SubjectNotFound;
import com.notesSharingApp.notesSharingApp.Service.AdminService;
import com.notesSharingApp.notesSharingApp.model.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // Getting count of pending user info update profiles, reported profiles and
    //  approval notes
    @GetMapping("/count")
    public ResponseEntity<?> getCountOfPendingNotes_UserUpdates_ReportedUsersProfiles(){
        try {
            return ResponseEntity.ok(adminService.getCounts());
        }catch (RuntimeException ex){
            ex.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal Server error");
        }
    }
}
