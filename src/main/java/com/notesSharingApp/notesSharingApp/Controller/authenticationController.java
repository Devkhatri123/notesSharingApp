package com.notesSharingApp.notesSharingApp.Controller;


import com.notesSharingApp.notesSharingApp.DTO.jsonResponse;
import com.notesSharingApp.notesSharingApp.DTO.loginDTO;
import com.notesSharingApp.notesSharingApp.DTO.verificationDTO;
import com.notesSharingApp.notesSharingApp.Exception.AccountIsDisabled;
import com.notesSharingApp.notesSharingApp.Exception.AccountNotFound;
import com.notesSharingApp.notesSharingApp.Exception.VerificationCodeExpired;
import com.notesSharingApp.notesSharingApp.Service.authenticationService;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import com.notesSharingApp.notesSharingApp.model.user;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.notesSharingApp.notesSharingApp.Service.jwtService;

import java.util.ArrayList;
import java.util.List;


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
        jsonResponse response = new jsonResponse();
        try {
            authenticationService.register(user);
            response.setMessage("Registration successful. Verification code sent to your email");
            response.setHttpStatusCode(HttpStatus.CREATED);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (MessagingException e) {
            response.setMessage("Something went wrong");
            response.setHttpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            response.setMessage(e.getMessage());
            response.setHttpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
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
    public ResponseEntity<List<Object>> login(@RequestBody loginDTO loginDto) {
         user user = null;
         jsonResponse jsonResponse = new jsonResponse();
         List<Object> objectList = new ArrayList<>();
         try {
              user =  authenticationService.login(loginDto);
              String token = jwtService.generateToken(user);
              jsonResponse.setMessage(token);
              jsonResponse.setHttpStatusCode(HttpStatus.OK);
              objectList.add(user);
              objectList.add(jsonResponse);
              return new ResponseEntity<>(objectList, HttpStatus.OK);
        }catch (RuntimeException e) {
             System.out.println(e.getMessage());
             jsonResponse.setMessage(e.getMessage());
             jsonResponse.setHttpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
             objectList.add(jsonResponse);
             return new ResponseEntity<>(objectList,HttpStatus.INTERNAL_SERVER_ERROR);
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
}
