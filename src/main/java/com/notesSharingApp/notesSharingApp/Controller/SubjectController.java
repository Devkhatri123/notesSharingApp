package com.notesSharingApp.notesSharingApp.Controller;

import com.notesSharingApp.notesSharingApp.DTO.SubjectResponseDTO;
import com.notesSharingApp.notesSharingApp.DTO.jsonResponse;
import com.notesSharingApp.notesSharingApp.model.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.notesSharingApp.notesSharingApp.Service.SubjectService;

import java.util.List;


@RestController
@RequestMapping("/v1/subject")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllSubject(@RequestParam(name = "pageNumber") Integer pageNumber, @RequestParam(name = "pageSize") Integer pageSize,@RequestParam(name = "query") String query){
        jsonResponse response = new jsonResponse();
        try {
            return new ResponseEntity<>(subjectService.getAllSubject(pageNumber, pageSize,query),HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
            response.setMessage(e.getMessage());
            response.setHttpStatusCode(HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(response,response.getHttpStatusCode());
        }
    }
    @GetMapping("/adminDepartmentSubjects")
    public ResponseEntity<?> getAllSubjectOfUserDepartment(@RequestParam(name = "pageNumber") Integer pageNumber, @RequestParam(name = "pageSize") Integer pageSize,@RequestParam(name = "query") String query,@RequestParam(name = "department") String department){
        List<SubjectResponseDTO> subjects = subjectService.getAllSubjectOfUserDepartment(pageNumber,pageSize,query,department);
        return ResponseEntity.ok(subjects);
    }

}
