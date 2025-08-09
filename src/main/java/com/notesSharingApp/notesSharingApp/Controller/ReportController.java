package com.notesSharingApp.notesSharingApp.Controller;

import com.notesSharingApp.notesSharingApp.DTO.UserReportDTO;
import com.notesSharingApp.notesSharingApp.Service.UserReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/report")
public class ReportController {

    @Autowired
    private UserReportService reportService;

    @PostMapping("/user")
    public ResponseEntity<?> report(@RequestBody UserReportDTO userReportDTO){
       reportService.reportUser(userReportDTO);
       return ResponseEntity.ok("Reported successfully");
    }
}
