package com.notesSharingApp.notesSharingApp.Controller;

import com.notesSharingApp.notesSharingApp.DTO.jsonResponse;
import com.notesSharingApp.notesSharingApp.model.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.notesSharingApp.notesSharingApp.Service.subjectService;


@RestController
@PreAuthorize("hasRole('ROLE_STUDENT')")
@RequestMapping("v1/subject")
public class subjectController {

    @Autowired
    private subjectService subjectService;
    @GetMapping("/all")
    public ResponseEntity<?> getAllSubjectOfUserDepartment(){
        jsonResponse response = new jsonResponse();
        try {
            return new ResponseEntity<>(subjectService.getAllSubjectOfUserDepartmentAndSemester(),HttpStatus.FOUND);
        } catch (RuntimeException e) {
            response.setMessage(e.getMessage());
            response.setHttpStatusCode(HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(response,response.getHttpStatusCode());
        }
    }
}
