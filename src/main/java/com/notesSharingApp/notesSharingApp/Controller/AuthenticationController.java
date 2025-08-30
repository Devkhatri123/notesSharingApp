package com.notesSharingApp.notesSharingApp.Controller;


import com.notesSharingApp.notesSharingApp.DTO.RegisterDTO;
import com.notesSharingApp.notesSharingApp.DTO.ResetPasswordDTO;
import com.notesSharingApp.notesSharingApp.DTO.LoginDTO;
import com.notesSharingApp.notesSharingApp.DTO.VerificationDTO;
import com.notesSharingApp.notesSharingApp.Exception.*;
import com.notesSharingApp.notesSharingApp.Exception.Account.*;
import com.notesSharingApp.notesSharingApp.Service.AuthenticationService;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import com.notesSharingApp.notesSharingApp.Util.util;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/v1/auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;
    Map<String,Object> response = new HashMap<>();


    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@RequestBody RegisterDTO registerDTO) {
        try {
            authenticationService.register(registerDTO);
            return ResponseEntity.ok().body("Registration successful. Verification code sent to your university web mail");
        }catch (InvalidDepartment e){
            return ResponseEntity.badRequest().body("Invalid department selected");
        }catch (EmailAlreadyInUse e){
            return ResponseEntity.badRequest().body("Email is already in use");
        }catch (MessagingException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Something went wrong");
        }catch (UsernameAlreadyTaken e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        catch (RuntimeException e) {
             e.printStackTrace();
            return ResponseEntity.internalServerError().body("Something went wrong");
        }

    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerificationDTO verificationDTO) {
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
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDto, HttpServletResponse res) {
         try {
         return ResponseEntity.ok(util.convertUserModelToDTO(authenticationService.login(loginDto,res)));
        }catch (RuntimeException e) {
             if(e instanceof AccountNotFound || e instanceof EmailNotValid || e instanceof BadCredentialsException){
                 return ResponseEntity.badRequest().body(e.getMessage());
             }
             e.printStackTrace();
             return ResponseEntity.internalServerError().body("Internal Server error");
        }

     }
     @PostMapping("/resetPasswordToken/{email}")
     public ResponseEntity<String> resetPasswordToken(@PathVariable String email){
        try{
          authenticationService.createResetPasswordToken(email);
          return ResponseEntity.ok().body("Reset password link sent to your web mail");
        }catch (MessagingException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error in sending reset link");
        }catch (AccountNotFound e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (RuntimeException e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal server error");
        }
     }
     @PutMapping("/resetPassword")
     public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String email, @RequestBody ResetPasswordDTO resetPasswordDTO){
        try{
            authenticationService.resetPassword(token,email,resetPasswordDTO);
           return ResponseEntity.ok().body("Password changed successfully");
        } catch (RuntimeException e){
            e.printStackTrace();
            if(e instanceof AccountNotFound || e instanceof InvalidToken || e instanceof TokenExpired || e instanceof TokenNotFound){
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            return ResponseEntity.internalServerError().body("Something went wrong. Password reset operation failed. Try again later");
        }
     }
    @PostMapping("/resendVerificationCode")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email){
        try {
            authenticationService.resendVerificationCode(email);
            return ResponseEntity.ok("Verification code sent to your email");
        } catch (MessagingException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error in sending email");
        } catch (RuntimeException e) {
            if(e instanceof EmailNotValid || e instanceof  AccountVerified || e instanceof AccountNotFound){
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal Server error");
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
