package com.notesSharingApp.notesSharingApp.Controller;

import com.notesSharingApp.notesSharingApp.DTO.SubjectRequestDTO;
import com.notesSharingApp.notesSharingApp.DTO.SubjectResponseDTO;
import com.notesSharingApp.notesSharingApp.DTO.jsonResponse;
import com.notesSharingApp.notesSharingApp.Exception.Account.NotLoggedIn;
import com.notesSharingApp.notesSharingApp.Exception.CharacterLimitExceeded;
import com.notesSharingApp.notesSharingApp.Exception.Subject.SubjectAlreadyExists;
import com.notesSharingApp.notesSharingApp.Exception.Subject.SubjectNotFound;
import com.notesSharingApp.notesSharingApp.model.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.notesSharingApp.notesSharingApp.Service.SubjectService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/v1/subject")
public class SubjectController {
    Map<String,Object> response = new HashMap<>();

    @Autowired
    private SubjectService subjectService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllSubject(@RequestParam(name = "pageNumber") Integer pageNumber, @RequestParam(name = "pageSize") Integer pageSize,@RequestParam(name = "query") String query){
        try {
            return ResponseEntity.ok().body(subjectService.getAllSubject(pageNumber, pageSize,query));
        } catch (RuntimeException e) {
            e.printStackTrace();
           return ResponseEntity.internalServerError().body("Internal Server error");
        }
    }
    // Fetching admin's department subjects
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
   @GetMapping("/adminDepartmentSubjects")
    public ResponseEntity<?> getAllSubjectOfUserDepartment(@RequestParam(name = "pageNumber") Integer pageNumber, @RequestParam(name = "pageSize") Integer pageSize,@RequestParam(name = "query") String query,@RequestParam(name = "department") String department){
        List<SubjectResponseDTO> subjects = subjectService.getAllSubjectOfUserDepartment(pageNumber,pageSize,query,department);
        return ResponseEntity.ok(subjects);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/admin/addSubject")
    public ResponseEntity<?> addSubject(@RequestBody SubjectRequestDTO subjectRequestDTO){
        try {
            response.put("message","subject created successfully");
            response.put("NewSubject",subjectService.addSubject(subjectRequestDTO));
            return ResponseEntity.ok(response);
        } catch (SubjectAlreadyExists e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (CharacterLimitExceeded e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error in creating new subject. Try again");
        }
    }

    // Updating subject
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/admin/updateSubject")
    public ResponseEntity<?> updateSubject(@RequestBody SubjectResponseDTO subject){
        try {
            subjectService.updateSubject(subject);
            return ResponseEntity.ok("Subject updated successfully");
        } catch (RuntimeException e) {
            if(e instanceof SubjectNotFound) return ResponseEntity.notFound().build();
            else if(e instanceof CharacterLimitExceeded || e instanceof NotLoggedIn) return ResponseEntity.badRequest().body(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal Server Error");
        }
    }
    // Deleting subject
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/admin/subject/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable String id){
        try{
            subjectService.deleteSubject(id);
            return ResponseEntity.ok("Subject deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body("Error in deleting subject");
        }
    }
}
