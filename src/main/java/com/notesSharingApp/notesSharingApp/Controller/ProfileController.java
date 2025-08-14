package com.notesSharingApp.notesSharingApp.Controller;

import com.notesSharingApp.notesSharingApp.DTO.RemarkRequest;
import com.notesSharingApp.notesSharingApp.Service.ProfileService;
import com.notesSharingApp.notesSharingApp.model.TempUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/profile")
public class ProfileController {
    Map<String,Object> response = new HashMap<>();
    @Autowired
    private ProfileService profileService;


    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@RequestBody TempUser user, @PathVariable String userId){
        Map<String,Object> response = new HashMap<>();
        try {
            profileService.updateUser(userId, user);
            response.put("message","Update request has been sent, update will shown in few days once it is approved by admin");
            response.put("status", HttpStatus.OK.value());
            return ResponseEntity.ok().body(response);
        } catch (RuntimeException e) {
            response.put("message",e.getMessage());
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("admin/ApprovalPendingUserInfo")
    public List<TempUser> getApprovalPendingUsersInfo(@RequestParam(name = "pageNumber") Integer pageNumber, @RequestParam(name = "limit") Integer limit){
        return profileService.getApprovalPendingUsersInfo(pageNumber,limit);
    }

    @GetMapping("/UserInfoUpdateRequestStatus/{userID}")
    public ResponseEntity<?> getUserInfoUpdateRequestStatus(@PathVariable String userID){
        Map<String,String> statusMap = profileService.getUserInfoUpdateRequestStatus(userID);
        if(statusMap == null) return new ResponseEntity<>("User not found",HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(profileService.getUserInfoUpdateRequestStatus(userID));
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/admin/RejectInfoUpdateRequest/{userId}")
    public ResponseEntity<String> rejectUpdateInfoRequest(@PathVariable String userId, @RequestBody RemarkRequest remarkRequest){
        try {
            profileService.rejectUpdateInfoRequest(userId, remarkRequest);
            return ResponseEntity.ok("Operation completed");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return null;
    }
    @DeleteMapping("/UpdateInfoInfo/{userID}")
    public ResponseEntity<?> deleteUpdateInfoRequest(@PathVariable String userID){
        profileService.deleteUpdateInfoRequest(userID);
        return ResponseEntity.ok("Request Deleted successfully");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/admin/approveChanges/{userId}")
    public ResponseEntity<String> approveUserInfoUpdateRequest(@PathVariable String userId){
        profileService.approveChanges(userId);
        return ResponseEntity.ok("Changes applied to user profile");
    }
    @PostMapping("/admin/block/user/{userId}")
    public ResponseEntity<?> blockUser(@PathVariable String userId){
        try {
            profileService.blockUser(userId);
            response.put("message","user blocked successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("message","user blocked successfully");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
