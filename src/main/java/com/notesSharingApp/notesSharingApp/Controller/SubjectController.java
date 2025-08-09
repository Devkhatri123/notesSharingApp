package com.notesSharingApp.notesSharingApp.Controller;

import com.notesSharingApp.notesSharingApp.DTO.jsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.notesSharingApp.notesSharingApp.Service.SubjectService;


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
            response.setMessage(e.getMessage());
            response.setHttpStatusCode(HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(response,response.getHttpStatusCode());
        }
    }

}
