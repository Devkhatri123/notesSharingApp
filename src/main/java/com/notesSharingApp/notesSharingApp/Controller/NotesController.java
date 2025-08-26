package com.notesSharingApp.notesSharingApp.Controller;


import com.notesSharingApp.notesSharingApp.DTO.UploadNoteDTO;
import com.notesSharingApp.notesSharingApp.DTO.jsonResponse;
import com.notesSharingApp.notesSharingApp.DTO.notesDTO;
import com.notesSharingApp.notesSharingApp.Exception.*;
import com.notesSharingApp.notesSharingApp.Exception.Account.AccountIsBlocked;
import com.notesSharingApp.notesSharingApp.Exception.Account.AccountIsDisabled;
import com.notesSharingApp.notesSharingApp.Exception.Account.AccountNotFound;
import com.notesSharingApp.notesSharingApp.Exception.Account.EmailNotVerified;
import com.notesSharingApp.notesSharingApp.Exception.Subject.SubjectNotFound;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.notesSharingApp.notesSharingApp.Service.NotesService;
import com.notesSharingApp.notesSharingApp.DTO.RemarkRequest;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/v1/notes")
public class NotesController {

    private final NotesService notesService;
    final Map<String,String> response = new HashMap<>();

    @Autowired
    public NotesController(NotesService notesService){
        this.notesService = notesService;
    }


    @PostMapping(value = "/uploadNote",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> uploadNotes(
                               @RequestPart(value = "thumbnail") MultipartFile thumbnail,
                               @RequestPart(value = "notes") MultipartFile notes,
                               @RequestPart(value = "note") UploadNoteDTO note

    ){
        Map<String,Object> response = new HashMap<>();
         try {
            notesService.uploadNote(thumbnail,notes,note);
            response.put("message","Notes sent to admin for review. It will be available to users within 2-3 days if everything is ok in notes");
            response.put("status",HttpStatus.CREATED.value());
            return ResponseEntity.ok().body(response);
        }catch (IOException e) {
             response.put("message",e.getMessage());
             e.printStackTrace();
             return ResponseEntity.internalServerError().body(response);
        }
         catch (RuntimeException e) {
             response.put("message",e.getMessage());
             if(e instanceof CharacterLimitExceeded || e instanceof AccountIsBlocked || e instanceof AccountIsDisabled
                     || e instanceof FileNotSupported || e instanceof EmailNotVerified
                     || e instanceof SubjectNotFound || e instanceof AccountNotFound) {
                 return ResponseEntity.badRequest().body(response);
             }else response.put("message","Internal Server error. Note couldn't be uploaded, try again");
             e.printStackTrace();
             return ResponseEntity.internalServerError().body(response);
        }

    }
    @GetMapping
    public ResponseEntity<?> getSubjectNotes(@RequestParam String subjectID,@RequestParam(name = "pageNumber") Integer pageNumber,@RequestParam(name = "limit") Integer limit,@RequestParam(name = "query") String query){
        jsonResponse response = new jsonResponse();
        try {
        return new ResponseEntity<>( notesService.getSubjectNotes(subjectID,pageNumber,limit,query), HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            response.setMessage("Internal Server Error");
            response.setHttpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            return new ResponseEntity<>(response, response.getHttpStatusCode());
        }
    }
    @GetMapping("/note/{noteID}")
    public ResponseEntity<?> getNote(@PathVariable String noteID){
        try {
            return new ResponseEntity<>(notesService.getNote(noteID),HttpStatus.OK);
        }catch (NoteNotFound e) {
            return ResponseEntity.notFound().build();
        }catch (NoSuchElementException e) {
             return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body("Internal server error. Try again");
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("admin/ApprovalPendingNotes")
    public ResponseEntity<?> getApprovalPendingNotes(@RequestParam(name = "pageNumber") Integer pageNumber,@RequestParam(name = "limit") Integer limit){
        try {
            return ResponseEntity.ok(notesService.getApprovalPendingNotes(pageNumber, limit));
        }catch (NotAllowed e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.FORBIDDEN);
        }catch (RuntimeException e){
            return ResponseEntity.internalServerError().body("Internal server error");
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("admin/sendRemarkForNote")
    public jsonResponse sendRemark(@RequestBody RemarkRequest request) {
        jsonResponse response = new jsonResponse();
        try {
            notesService.sendRemark(request);
            response.setMessage("Remark sent successfully");
            response.setHttpStatusCode(HttpStatus.OK);
        } catch (MessagingException e) {
            response.setMessage(e.getMessage());
            response.setHttpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @GetMapping("/myNotes")
    public ResponseEntity<?> getMyNotes(@RequestParam String userId,@RequestParam(name = "status") String status, @RequestParam(name = "pageNumber") Integer pageNumber, @RequestParam(name = "limit") Integer limit){
        Map<String,Object> response = new HashMap<>();
        response.put("myNotes",notesService.myNotes(userId,status,pageNumber,limit));
        response.put("count",notesService.getCountsOfNotes(userId));
        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
   @PostMapping("/{noteID}/approve")
   public ResponseEntity<?> approveNote(@PathVariable String noteID) {
        jsonResponse response = new jsonResponse();
        try {
            notesService.approveNote(noteID);
            response.setMessage("Note approved!!!");
            response.setHttpStatusCode(HttpStatus.OK);
            return new ResponseEntity<>(response, response.getHttpStatusCode());
        } catch (NoteNotFound e) {
            e.printStackTrace();
            response.setMessage(e.getMessage());
            response.setHttpStatusCode(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(response, response.getHttpStatusCode());
        }catch (RuntimeException e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Something went wrong");
        }
    }

       @DeleteMapping("/{noteID}")
       public ResponseEntity<?> deleteNote(@PathVariable String noteID){
         notesService.deleteNote(noteID);
         return ResponseEntity.ok("Note deleted successfully!");
       }

}
