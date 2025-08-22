package com.notesSharingApp.notesSharingApp.Controller;


import com.notesSharingApp.notesSharingApp.DTO.RemarkRequest;
import com.notesSharingApp.notesSharingApp.DTO.jsonResponse;
import com.notesSharingApp.notesSharingApp.DTO.loginDTO;
import com.notesSharingApp.notesSharingApp.DTO.verificationDTO;
import com.notesSharingApp.notesSharingApp.Exception.*;
import com.notesSharingApp.notesSharingApp.Service.AuthenticationService;
import com.notesSharingApp.notesSharingApp.model.TempUser;
import com.notesSharingApp.notesSharingApp.model.User;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.notesSharingApp.notesSharingApp.Util.util;
import com.notesSharingApp.notesSharingApp.Service.JwtService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/v1/auth")
public class AuthenticationController {
    @Value("${spring.mail.username}")
    private String username;
    @Value("${spring.mail.password}")
    private String password;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private JwtService jwtService;

    Map<String,Object> response = new HashMap<>();


    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@RequestBody User user) {
        try {
            authenticationService.register(user);
            response.put("message","Registration successful. Verification code sent to your university web mail");
            return ResponseEntity.ok().body(response);
        } catch (MessagingException e) {
            response.put("message","Something went wrong");
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(response);
        } catch (RuntimeException e) {
             response.put("message", "something wrong on server. Registration operation halted. Try again");
             e.printStackTrace();
            return ResponseEntity.internalServerError().body(response);
        }

    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody verificationDTO verificationDTO) {
        try {
            authenticationService.verify(verificationDTO);
            return ResponseEntity.ok().body("Verification successful!");
        } catch (RuntimeException ex) {
            if (ex instanceof AccountNotFound || ex instanceof VerificationCodeExpired ||
            ex instanceof VerificationCodeNotMatched || ex instanceof EmailNotValid ||
            ex instanceof AccountVerified) {
                return ResponseEntity.badRequest().body(ex.getMessage());
            }
            return ResponseEntity.internalServerError().body("Internal Server error");
    }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody loginDTO loginDto, HttpServletResponse res) {
         try {
             User user = authenticationService.login(loginDto,res);
             response.put("user",util.convertUserModelToDTO(user));
             response.put("Status",200);
             return ResponseEntity.ok(response);
        }catch (RuntimeException e) {
             e.printStackTrace();
             response.put("message",e.getMessage());
             return ResponseEntity.badRequest().body(response);
        }

     }
    @PostMapping("/resendVerificationCode")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email){
        try {
            authenticationService.resendVerificationCode(email);
            response.put("message","Verification code sent to your email");
            response.put("status",HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (MessagingException e) {
            response.put("message",e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (AccountNotFound e){
            response.put("message",e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            response.put("message",e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // Getting loggedInUser state for every request on frontend. This will help to find Out whether the
    // token is valid and user is loggedIn if token found expired or we don't currently loggedInUser
    // from Security Context then we will log out user from frontend.
     @GetMapping("/loggedInUser")
     public ResponseEntity<?> getLoggedInUser(){

        userdetails user = authenticationService.getLoggedInUser();
        if(user != null){
            response.put("user",util.convertUserModelToDTO(user.getUser()));
            response.put("isLoggedIn",true);
            return ResponseEntity.ok(response);
        }
         response.put("user",null);
         response.put("isLoggedIn",false);
         response.put("Status",404);
         return ResponseEntity.ok(response);
  }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse res) {
        authenticationService.logout(res);
        response.put("user", null);
        response.put("isLoggedIn", false);
        response.put("Status", 200);
        return ResponseEntity.ok(response);
    }
}
