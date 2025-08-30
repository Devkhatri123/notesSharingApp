package com.notesSharingApp.notesSharingApp.Controller;

import com.notesSharingApp.notesSharingApp.DTO.RemarkRequest;
import com.notesSharingApp.notesSharingApp.DTO.UserDTOWithoutNotes;
import com.notesSharingApp.notesSharingApp.Exception.Account.*;
import com.notesSharingApp.notesSharingApp.Exception.CharacterLimitExceeded;
import com.notesSharingApp.notesSharingApp.Exception.DecisionAlreadyMade;
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
            return ResponseEntity.ok(profileService.getUserProfile(userId));
        } catch (RuntimeException e) {
            if (e instanceof AccountNotFound) return ResponseEntity.notFound().build();
            return ResponseEntity.internalServerError().body("Internal Server error");
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateProfile(@RequestBody TempUser user, @PathVariable String userId){
        try {
            profileService.updateUser(userId, user);
            return ResponseEntity.ok().body("Update request has been sent, update will shown in few days once it is approved by admin");
        }catch (EmailNotValid ex){
            return ResponseEntity.badRequest().body(ex.getMessage());
        }catch (EmailAlreadyInUse ex){
            return ResponseEntity.badRequest().body(ex.getMessage());
        }catch (AccountNotFound ex){
           return ResponseEntity.notFound().build();
        }catch (UsernameAlreadyTaken ex){
            return ResponseEntity.badRequest().body(ex.getMessage());
        }catch (RuntimeException ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError().body("Something went wrong on server");
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/all")
    public List<UserDTOWithoutNotes> getProfiles(@RequestParam(name = "query") String query, @RequestParam(name = "pageNumber") Integer pageNumber, @RequestParam(name = "limit") Integer limit){
        return profileService.getProfiles(query,pageNumber,limit);
    }
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("admin/ApprovalPendingUserInfo")
    public List<TempUser> getApprovalPendingUsersInfo(@RequestParam(name = "pageNumber") Integer pageNumber, @RequestParam(name = "limit") Integer limit){
        return profileService.getApprovalPendingUsersInfo(pageNumber,limit);
    }

    @GetMapping("/UserInfoUpdateRequestStatus/{userID}")
    public ResponseEntity<?> getUserInfoUpdateRequestStatus(@PathVariable String userID){
        return ResponseEntity.ok(profileService.getUserInfoUpdateRequestStatus(userID));
    }


    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/admin/RejectInfoUpdateRequest/{userId}")
    public ResponseEntity<String> rejectUpdateInfoRequest(@PathVariable String userId, @RequestBody RemarkRequest remarkRequest){
        try {
            profileService.rejectUpdateInfoRequest(userId, remarkRequest);
            return ResponseEntity.ok("Operation completed");
        } catch (DecisionAlreadyMade e) {
           return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AccountNotFound e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (CharacterLimitExceeded e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Something went wrong");
        }
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
            return ResponseEntity.internalServerError().body("Something went wrong in sending otp. Try again");
        } catch (DecisionAlreadyMade e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
            return ResponseEntity.ok("user blocked successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body("Internal Server error.");
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/admin/unblock/user/{userId}")
    public ResponseEntity<?> unBlockUser(@PathVariable String userId){
        try {
            profileService.unBlockUser(userId);
            return ResponseEntity.ok("user unblocked successfully");
        }catch (AccountNotFound e){
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body("Error in unblocking user. Try again");
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/admin/activate/user/{userId}")
    public ResponseEntity<?> activateProfile(@PathVariable String userId){
        try{
            profileService.activateProfile(userId);
            return ResponseEntity.ok("Profile reactivated!");
        } catch (AccountNotFound e) {
          return ResponseEntity.notFound().build();
        } catch (AccountIsDisabled e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AccountVerified e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body("Internal Server error");
        }
    }
}
