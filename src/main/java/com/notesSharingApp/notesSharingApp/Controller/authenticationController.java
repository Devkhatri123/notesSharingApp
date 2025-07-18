package com.notesSharingApp.notesSharingApp.Controller;


import com.notesSharingApp.notesSharingApp.DTO.*;
import com.notesSharingApp.notesSharingApp.Exception.AccountIsDisabled;
import com.notesSharingApp.notesSharingApp.Exception.AccountNotFound;
import com.notesSharingApp.notesSharingApp.Exception.VerificationCodeExpired;
import com.notesSharingApp.notesSharingApp.Service.authenticationService;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import com.notesSharingApp.notesSharingApp.model.user;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.notesSharingApp.notesSharingApp.Service.jwtService;

import java.util.ArrayList;
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
            response.put("message","Something went wrong");
            response.put("status",HttpStatus.INTERNAL_SERVER_ERROR.value());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(response);
        }

    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody verificationDTO verificationDTO) {
        jsonResponse response = new jsonResponse();
        try {
            authenticationService.verify(verificationDTO);
            response.setMessage("Verification successful!");
            response.setHttpStatusCode(HttpStatus.OK);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (VerificationCodeExpired e) {
            response.setMessage(e.getMessage());
            response.setHttpStatusCode(HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            response.setMessage(e.getMessage());
            response.setHttpStatusCode(HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody loginDTO loginDto, HttpServletResponse res) {
         Map<String,Object> response = new HashMap<>();
         try {
              authenticationService.login(loginDto,res);
              response.put("isLoggedIn",true);
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
     @GetMapping("/loggedInUser")
     public userDTOWithoutNotes getLoggedInUser(){
        return authenticationService.getLoggedInUser();
  }
}
