package com.notesSharingApp.notesSharingApp.Controller;


import com.notesSharingApp.notesSharingApp.DTO.jsonResponse;
import com.notesSharingApp.notesSharingApp.DTO.notesDTO;
import com.notesSharingApp.notesSharingApp.model.Note;
import com.notesSharingApp.notesSharingApp.model.Subject;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.notesSharingApp.notesSharingApp.Service.notesService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/v1/notes")
public class notesController {

    @Autowired
    private notesService notesService;
    @Autowired
    adminController adminController;

    @PostMapping(value = "/uploadNote",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadNotes(@RequestPart(value = "thumbnail") MultipartFile thumbnail,
                               @RequestPart(value = "notes") MultipartFile notes,
                               @RequestPart Note note

    ){
        jsonResponse jsonResponse = new jsonResponse();
        try {
            notesService.uploadNote(thumbnail,notes,note);
            jsonResponse.setMessage("Notes sent to admin for review. It will be available to users within 2-3 days if everything is ok in notes");
            jsonResponse.setHttpStatusCode(HttpStatus.OK);
            return new ResponseEntity<>(jsonResponse,HttpStatus.OK);
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("error = " + e.getMessage());
            jsonResponse.setMessage(e.getMessage());
            jsonResponse.setHttpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return new ResponseEntity<>(jsonResponse,jsonResponse.getHttpStatusCode());
        }
    }
    @GetMapping("/")
    public List<notesDTO> getSubjectNotes(@RequestParam String subjectID){
        return notesService.getSubjectNotes(subjectID);
    }
}
