package com.notesSharingApp.notesSharingApp.Controller;

import com.notesSharingApp.notesSharingApp.DTO.RemarkRequest;
import com.notesSharingApp.notesSharingApp.DTO.userDTOWithoutNotes;
import com.notesSharingApp.notesSharingApp.Exception.AccountNotFound;
import com.notesSharingApp.notesSharingApp.Exception.EmailAlreadyInUse;
import com.notesSharingApp.notesSharingApp.Exception.EmailNotValid;
import com.notesSharingApp.notesSharingApp.Service.ProfileService;
import com.notesSharingApp.notesSharingApp.model.TempUser;
import jakarta.mail.MessagingException;
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

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable String userId){

        try {
            response.put("profile",profileService.getUserProfile(userId));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("message", e.getMessage());
            ResponseEntity.notFound();
            if (e instanceof AccountNotFound) return ResponseEntity.notFound().build();
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@RequestBody TempUser user, @PathVariable String userId){
        try {
            profileService.updateUser(userId, user);
            return ResponseEntity.ok().body("Update request has been sent, update will shown in few days once it is approved by admin");
        }catch (EmailNotValid ex){
            return ResponseEntity.badRequest().body(ex.getMessage());
        }catch (EmailAlreadyInUse ex){
            return ResponseEntity.badRequest().body(ex.getMessage());
        }catch (AccountNotFound ex){
           return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body("Something went wrong on server");
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/all")
    public List<userDTOWithoutNotes> getProfiles(@RequestParam(name = "query") String query,@RequestParam(name = "pageNumber") Integer pageNumber,@RequestParam(name = "limit") Integer limit){
        return profileService.getProfiles(query,pageNumber,limit);
    }
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
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


    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
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

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/admin/approveChanges/{userId}")
    public ResponseEntity<String> approveUserInfoUpdateRequest(@PathVariable String userId){
        try {
            profileService.approveChanges(userId);
            return ResponseEntity.ok("Changes applied to user profile");
        } catch (MessagingException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Something went wrong in sending otp");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Something went wrong on server");
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
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
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/admin/unblock/user/{userId}")
    public ResponseEntity<?> unBlockUser(@PathVariable String userId){
        profileService.unBlockUser(userId);
        response.put("message","user unblocked successfully");
        return ResponseEntity.ok(response);
    }
}
