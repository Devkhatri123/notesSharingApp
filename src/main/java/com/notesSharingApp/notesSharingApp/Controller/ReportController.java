package com.notesSharingApp.notesSharingApp.Controller;

import com.notesSharingApp.notesSharingApp.DTO.ReportedUserDTO;
import com.notesSharingApp.notesSharingApp.DTO.ReportRequestDTO;
import com.notesSharingApp.notesSharingApp.DTO.ReportResponseDTO;
import com.notesSharingApp.notesSharingApp.Exception.Account.AccountNotFound;
import com.notesSharingApp.notesSharingApp.Exception.Account.NotLoggedIn;
import com.notesSharingApp.notesSharingApp.Exception.NoteNotFound;
import com.notesSharingApp.notesSharingApp.Service.NoteReportService;
import com.notesSharingApp.notesSharingApp.Service.NotesService;
import com.notesSharingApp.notesSharingApp.Service.UserReportService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1")
public class ReportController {
    Map<String,Object> response = new HashMap<>();
    @Autowired
    private UserReportService userReportService;
    @Autowired
    private NoteReportService noteReportService;
    @Autowired
    private NotesService notesService;

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/report/user")
    public ResponseEntity<?> report(@RequestBody ReportRequestDTO reportRequestDTO){
        try {
            userReportService.reportUser(reportRequestDTO);
            return ResponseEntity.ok("Reported successfully");
        } catch (RuntimeException e) {
            if(e instanceof AccountNotFound || e instanceof NotLoggedIn){
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal server error. Try again");
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/report/admin/profile/all")
    public ResponseEntity<?> getAllReports(@RequestParam(name = "pageNumber") Integer pageNumber, @RequestParam(name = "limit") Integer limit){
        try {
            List<ReportedUserDTO> reports = userReportService.getReportedProfiles(pageNumber, limit);
            response.put("reports", reports);
            response.put("count", reports.size());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal Server error");
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/admin/user/{userId}/reports")
    public ResponseEntity<?> getUserReports(@PathVariable String userId,@RequestParam(name = "pageNumber") Integer pageNumber, @RequestParam(name = "limit") Integer limit){
        try {
            return ResponseEntity.ok(userReportService.getUserReports(userId, pageNumber, limit));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal Server error");
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/admin/user/{userId}/reports")
    public ResponseEntity<String> deleteUserReports(@PathVariable String userId){
        try {
            userReportService.deleteUserReports(userId);
            return ResponseEntity.ok("Report deleted successfully");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal server error");
        }
    }

    // Report Note Section
   // @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/report/note")
    public ResponseEntity<?> reportNote(@RequestBody ReportRequestDTO reportNoteRequestDTO){
       try {
           noteReportService.reportNote(reportNoteRequestDTO);
           return ResponseEntity.ok().body("Report has been sent successfully!!!");
       } catch (NoteNotFound e) {
           return ResponseEntity.notFound().build();
       }catch (AccountNotFound e){
           return ResponseEntity.notFound().build();
       }
       catch (RuntimeException e){
           e.printStackTrace();
           return ResponseEntity.internalServerError().body("Internal server error");
       }
    }
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("report/note/all")
    public ResponseEntity<?> getAllReportedNote(@RequestParam Integer pageNumber,@RequestParam Integer limit){
        try{
           return ResponseEntity.ok(noteReportService.getAllReportedNote(pageNumber,limit));
        } catch (RuntimeException e) {
            e.printStackTrace();
           return ResponseEntity.internalServerError().body("Internal Server error");
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("report/note/{noteId}")
    public ResponseEntity<?> getNoteReports(@RequestParam Integer pageNumber,@RequestParam Integer limit,@PathVariable String noteId){
        try{
            return ResponseEntity.ok(noteReportService.getNoteReports(pageNumber,limit,noteId));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal Server error");
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/admin/report/note/{noteId}")
    public ResponseEntity<?> discardNoteReports(@PathVariable String noteId){
        try{
            noteReportService.discardReports(noteId);
            return ResponseEntity.ok("Reports Discarded successfully");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal Server error");
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/admin/remove/note/{noteId}")
    public ResponseEntity<?> dismissNote(@PathVariable String noteId,@RequestParam String noteRemovalReason){
        try{
            notesService.removeNote(noteId,noteRemovalReason);
            return ResponseEntity.ok("Note removed successfully");
        } catch (MessagingException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error in sending email to Note owner");
        } catch (NoteNotFound e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal Server error");
        }
    }
}
