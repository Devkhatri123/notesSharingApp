package com.notesSharingApp.notesSharingApp.Controller;


import com.notesSharingApp.notesSharingApp.DTO.UploadNoteDTO;
import com.notesSharingApp.notesSharingApp.Exception.*;
import com.notesSharingApp.notesSharingApp.Exception.Account.AccountIsBlocked;
import com.notesSharingApp.notesSharingApp.Exception.Account.AccountIsDisabled;
import com.notesSharingApp.notesSharingApp.Exception.Account.AccountNotFound;
import com.notesSharingApp.notesSharingApp.Exception.Account.EmailNotVerified;
import com.notesSharingApp.notesSharingApp.Exception.Note.FileNotSupported;
import com.notesSharingApp.notesSharingApp.Exception.Note.FileTooBig;
import com.notesSharingApp.notesSharingApp.Exception.Subject.SubjectNotFound;
import io.jsonwebtoken.security.Request;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import com.notesSharingApp.notesSharingApp.Service.NotesService;
import com.notesSharingApp.notesSharingApp.DTO.RemarkRequest;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/v1/notes")
public class NotesController {

    private final NotesService notesService;

    @Autowired
    public NotesController(NotesService notesService){
        this.notesService = notesService;
    }

    @CrossOrigin(originPatterns = {"https://study-share-eta.vercel.app"},methods = {RequestMethod.POST})
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
            return ResponseEntity.ok().body(response);
        }catch (IOException e) {
             e.printStackTrace();
             return ResponseEntity.internalServerError().body("Internal Server error");
         } catch (RuntimeException e) {
             e.printStackTrace();
             if(e instanceof CharacterLimitExceeded || e instanceof AccountIsBlocked || e instanceof AccountIsDisabled
                     || e instanceof FileNotSupported || e instanceof EmailNotVerified
                     || e instanceof SubjectNotFound || e instanceof AccountNotFound || e instanceof FileTooBig) {
                 return ResponseEntity.badRequest().body(e.getMessage());
             }else{
             return ResponseEntity.internalServerError().body("Internal Server error. Note couldn't be uploaded, try again");
             }
         }

    }
    @GetMapping
    public ResponseEntity<?> getSubjectNotes(@RequestParam String subjectID,@RequestParam(name = "pageNumber") Integer pageNumber,@RequestParam(name = "limit") Integer limit,@RequestParam(name = "query") String query){
        try {
        return new ResponseEntity<>(notesService.getSubjectNotes(subjectID,pageNumber,limit,query), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal Server Error");
        }
    }
    @GetMapping("/note/{noteID}")
    public ResponseEntity<?> getNote(@PathVariable String noteID){
        try {
            return ResponseEntity.ok(notesService.getNote(noteID));
        }catch (NoteNotFound e) {
            return ResponseEntity.notFound().build();
        }catch (NoSuchElementException e) {
             return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal server error.");
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
    public ResponseEntity<?> sendRemark(@RequestBody RemarkRequest request) {
        try {
            notesService.sendRemark(request);
            return ResponseEntity.ok("Remark sent successfully");
        } catch (MessagingException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Something went wrong. Try again later");
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Something went wrong");
        }
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
        try {
            notesService.approveNote(noteID);
            return ResponseEntity.ok("Note approved!!!");
        } catch (NoteNotFound e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
