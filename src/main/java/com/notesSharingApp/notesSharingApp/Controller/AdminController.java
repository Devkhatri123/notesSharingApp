package com.notesSharingApp.notesSharingApp.Controller;

import com.notesSharingApp.notesSharingApp.Service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/count")
    public ResponseEntity<?> getCountOfPendingNotes_UserUpdates_ReportedUsersProfiles(){
     return ResponseEntity.ok(adminService.getCounts());
    }
}
