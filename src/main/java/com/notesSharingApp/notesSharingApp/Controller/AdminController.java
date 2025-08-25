package com.notesSharingApp.notesSharingApp.Controller;

import com.notesSharingApp.notesSharingApp.DTO.SubjectRequestDTO;
import com.notesSharingApp.notesSharingApp.DTO.SubjectResponseDTO;
import com.notesSharingApp.notesSharingApp.Exception.Account.NotLoggedIn;
import com.notesSharingApp.notesSharingApp.Exception.CharacterLimitExceeded;
import com.notesSharingApp.notesSharingApp.Exception.Subject.SubjectAlreadyExists;
import com.notesSharingApp.notesSharingApp.Exception.Subject.SubjectNotFound;
import com.notesSharingApp.notesSharingApp.Service.AdminService;
import com.notesSharingApp.notesSharingApp.model.Subject;
import org.springframework.beans.factory.annotation.Autowired;
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
     return ResponseEntity.ok(adminService.getCounts());
    }
    // adding new subject
    @PostMapping("/addSubject")
    public ResponseEntity<?> addSubject(@RequestBody SubjectRequestDTO subjectRequestDTO){
        Map<String,Object> response = new HashMap<>();
        try {
            response.put("message","subject created successfully");
            response.put("NewSubject",adminService.addSubject(subjectRequestDTO));
            return ResponseEntity.ok(response);
        } catch (SubjectAlreadyExists e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error in creating new subject. Try again");
        }
    }
    // Updating subject
    @PutMapping("/updateSubject")
    public ResponseEntity<?> updateSubject(@RequestBody SubjectResponseDTO subject){
        try {
            adminService.updateSubject(subject);
            return ResponseEntity.ok("Subject updated successfully");
        } catch (RuntimeException e) {
            if(e instanceof SubjectNotFound) return ResponseEntity.notFound().build();
            else if(e instanceof CharacterLimitExceeded || e instanceof NotLoggedIn) return ResponseEntity.badRequest().body(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal Server Error");
        }
    }
    // Deleting subject
    @DeleteMapping("/subject/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable String id){
        try{
           adminService.deleteSubject(id);
           return ResponseEntity.ok("Subject deleted successfully");
        } catch (RuntimeException e) {
           return ResponseEntity.internalServerError().body("Error in deleting subject");
        }
    }
}
