package com.notesSharingApp.notesSharingApp.Controller;


import com.notesSharingApp.notesSharingApp.DTO.jsonResponse;
import com.notesSharingApp.notesSharingApp.DTO.loginDTO;
import com.notesSharingApp.notesSharingApp.DTO.userDTOWithoutNotes;
import com.notesSharingApp.notesSharingApp.DTO.verificationDTO;
import com.notesSharingApp.notesSharingApp.Exception.AccountNotFound;
import com.notesSharingApp.notesSharingApp.Exception.AccountVerified;
import com.notesSharingApp.notesSharingApp.Exception.VerificationCodeExpired;
import com.notesSharingApp.notesSharingApp.Service.authenticationService;
import com.notesSharingApp.notesSharingApp.model.TempUser;
import com.notesSharingApp.notesSharingApp.model.user;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.notesSharingApp.notesSharingApp.Service.jwtService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/v1/auth")
public class authenticationController {
    @Value("${spring.mail.username}")
    private String username;
    @Value("${spring.mail.password}")
    private String password;
    @Autowired
    private authenticationService authenticationService;
    @Autowired
    private jwtService jwtService;

    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@RequestBody user user) {
       Map<String,Object> response = new HashMap<>();
        try {
            authenticationService.register(user);
            response.put("message","Registration successful. Verification code sent to your university web mail");
            response.put("status",HttpStatus.CREATED.value());
            return ResponseEntity.ok().body(response);
        } catch (MessagingException e) {
            response.put("message","Something went wrong");
            response.put("status",HttpStatus.INTERNAL_SERVER_ERROR.value());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(response);
        } catch (RuntimeException e) {
            response.put("message",e.getMessage());
            response.put("status",HttpStatus.INTERNAL_SERVER_ERROR.value());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(response);
        }

    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody verificationDTO verificationDTO) {
        Map<String,Object> response = new HashMap<>();
        try {
            authenticationService.verify(verificationDTO);
            response.put("message","Verification successful!");
            response.put("status",HttpStatus.OK.value());
            return ResponseEntity.ok().body(response);
        } catch (VerificationCodeExpired e) {
            response.put("message",e.getMessage());
            response.put("status",HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        } catch (RuntimeException e) {
            if(e instanceof AccountVerified){
                response.put("status",HttpStatus.OK.value());
            }else response.put("status",HttpStatus.BAD_REQUEST.value());

            response.put("message",e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody loginDTO loginDto, HttpServletResponse res) {
         Map<String,Object> response = new HashMap<>();
         try {
             user user = authenticationService.login(loginDto,res);
             response.put("user",authenticationService.convertUserModelToDTO(user));
             response.put("Status",200);
             return ResponseEntity.ok(response);
        }catch (RuntimeException e) {
             e.printStackTrace();
             response.put("message",e.getMessage());
             response.put("Status",HttpStatus.BAD_REQUEST.value());
             return ResponseEntity.badRequest().body(response);
        }

     }
    @PostMapping("/resendVerificationCode")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email){
        jsonResponse response = new jsonResponse();
        try {
            authenticationService.resendVerificationCode(email);
            response.setMessage("Verification code sent to your email");
            response.setHttpStatusCode(HttpStatus.OK);
            return new ResponseEntity<>(response,response.getHttpStatusCode());
        } catch (MessagingException e) {
            response.setMessage(e.getMessage());
            response.setHttpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return new ResponseEntity<>(response,response.getHttpStatusCode());
        }catch (AccountNotFound e){
            response.setMessage(e.getMessage());
            response.setHttpStatusCode(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(response,response.getHttpStatusCode());
        } catch (RuntimeException e) {
            response.setMessage(e.getMessage());
            response.setHttpStatusCode(HttpStatus.OK);
            return new ResponseEntity<>(response,response.getHttpStatusCode());
        }
    }
    // Getting loggedInUser state for every request on frontend. This will help to find Out whether the
    // token is valid and user is loggedIn if token found expired or we don't currently loggedInUser
    // from Security Context then we will log out user from frontend.
     @GetMapping("/loggedInUser")
     public ResponseEntity<?> getLoggedInUser(){
        Map<String,Object> response = new HashMap<>();
        userdetails user = authenticationService.getLoggedInUser();
        if(user != null){
            response.put("user",authenticationService.convertUserModelToDTO(user.getUser()));
            response.put("isLoggedIn",true);
            response.put("Status",200);
            return ResponseEntity.ok(response);
        }
         response.put("user",null);
         response.put("isLoggedIn",false);
         response.put("Status",404);
         return ResponseEntity.ok(response);
  }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse res){
        authenticationService.logout(res);
        Map<String,Object> response = new HashMap<>();
        response.put("user",null);
        response.put("isLoggedIn",false);
        response.put("Status",200);
        return ResponseEntity.ok(response);
   }
       @PutMapping("/{userId}")
       public ResponseEntity<?> updateUser(@RequestBody TempUser user, @PathVariable  String userId){
        Map<String,Object> response = new HashMap<>();
        try {
            authenticationService.updateUser(userId, user);
            response.put("message","Update request has been sent, update will shown in few days once it is approved my admin");
            response.put("status",HttpStatus.OK.value());
            return ResponseEntity.ok().body(response);
        } catch (RuntimeException e) {
            response.put("message",e.getMessage());
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.internalServerError().body(response);
        }
       }
       @PreAuthorize("hasRole('ROLE_ADMIN')")
       @GetMapping("admin/ApprovalPendingUserInfo")
       public List<userDTOWithoutNotes> getApprovalPendingUsersInfo(){
       return null;
       }
}
