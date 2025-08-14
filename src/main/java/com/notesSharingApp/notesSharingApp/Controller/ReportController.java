package com.notesSharingApp.notesSharingApp.Controller;

import com.notesSharingApp.notesSharingApp.DTO.ReportedUserDTO;
import com.notesSharingApp.notesSharingApp.DTO.UserReportRequestDTO;
import com.notesSharingApp.notesSharingApp.DTO.ReportResponseDTO;
import com.notesSharingApp.notesSharingApp.Service.ReportService;
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
    private ReportService reportService;

    @PostMapping("/report/user")
    public ResponseEntity<?> report(@RequestBody UserReportRequestDTO userReportRequestDTO){
       reportService.reportUser(userReportRequestDTO);
       return ResponseEntity.ok("Reported successfully");
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/report/admin/profile/all")
    public ResponseEntity<?> getAllReports(@RequestParam(name = "pageNumber") Integer pageNumber, @RequestParam(name = "limit") Integer limit){
        List<ReportedUserDTO> reports = reportService.getAllReports(pageNumber,limit);
        response.put("reports",reports);
        response.put("count",reports.size());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/user/{userId}/reports")
    public List<ReportResponseDTO> getUserReports(@PathVariable String userId,@RequestParam(name = "pageNumber") Integer pageNumber, @RequestParam(name = "limit") Integer limit){
         return reportService.getUserReports(userId,pageNumber,limit);
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/admin/user/{userId}/reports")
    public ResponseEntity<String> deleteUserReports(@PathVariable String userId){
        try {
            reportService.deleteUserReports(userId);
            return ResponseEntity.ok("Report deleted successfully");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal server error");
        }
    }
}
