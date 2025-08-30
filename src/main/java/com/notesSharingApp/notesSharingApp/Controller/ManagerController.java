package com.notesSharingApp.notesSharingApp.Controller;

import com.notesSharingApp.notesSharingApp.Exception.Account.AccountNotFound;
import com.notesSharingApp.notesSharingApp.Service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(("/v1/manager"))
@PreAuthorize("hasRole('ROLE_MANAGER')")
public class ManagerController {

    @Autowired
    private ManagerService managerService;

    @PostMapping("/promoteUserToAdmin/{userId}")
    public ResponseEntity<?> promoteUserToAdmin(@PathVariable String userId){
        try {
            managerService.addAdminRole(userId);
            return ResponseEntity.ok("User promoted to admin successfully");
        }catch (AccountNotFound ex){
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal Server error");
        }
    }
    @PostMapping("/removeAdminRole/{userId}")
    public ResponseEntity<?> removeAdminRole(@PathVariable String userId){
        try {
            managerService.removeAdminRole(userId);
            return ResponseEntity.ok("User's admin role removed successfully");
        } catch (AccountNotFound e) {
           return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal server error");
        }
    }
}
