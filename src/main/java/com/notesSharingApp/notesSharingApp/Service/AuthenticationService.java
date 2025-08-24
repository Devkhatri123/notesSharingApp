package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.DTO.loginDTO;
import com.notesSharingApp.notesSharingApp.DTO.VerificationDTO;
import com.notesSharingApp.notesSharingApp.Exception.*;
import com.notesSharingApp.notesSharingApp.Util.util;
import com.notesSharingApp.notesSharingApp.model.*;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.notesSharingApp.notesSharingApp.repository.AuthenticationRepo;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthenticationService {
    private final String[] departments = {"CS","CE"};
    @Autowired
    private AuthenticationRepo authenticationRepo;
    @Autowired
    private EmailService emailService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;


    @Transactional
    public void register(User user) throws MessagingException,RuntimeException {
        if(!util.isValidEmail(user.getUniversityEmail())){
            throw new RuntimeException("University email is not valid");
        }else if (authenticationRepo.findByuniversityEmail(user.getUniversityEmail()) != null){
            throw new RuntimeException("Email is already in use");
        }else if(!Arrays.stream(departments).anyMatch(user.getDepartment()::equals)){
            throw new RuntimeException(user.getDepartment() +" department is not available right now");
        }


        // set UUId,encode password,role("student") and disable account until verification is done
        user.setId(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAccountStatus(AccountStatus.Active);
        user.setEmailVerified(false);
        user.setRoles(Set.of(Role.STUDENT));
       // user.setRole("STUDENT");
        user.setAccountRemarks("Your email is not verified");

        // Verification Code Logic
        user.setVerificationCode(generateVerificationCode());
        user.setExpirationAt(LocalDateTime.now().plusMinutes(15));
        User savedUser = authenticationRepo.save(user);
        if(authenticationRepo.findById(savedUser.getId()).isPresent()) {
            sendVerificationCode(user.getUniversityEmail(), user.getVerificationCode());
        }else{
            throw new RuntimeException("Failed in creating account.Please try again");
        }
    }
    // Generate Verification Code
    private int generateVerificationCode(){
        Random random = new Random();
        return random.nextInt(1000,9999);
    }
    private void sendVerificationCode(String to,int verificationCode) throws MessagingException {
        emailService.sendVerificationCode(to,verificationCode);
    }

    public void verify(VerificationDTO verificationDTO) throws VerificationCodeExpired,RuntimeException {
        if(verificationDTO.getEmail() == null){
            throw new EmailNotValid("Email not provided");
        }
        User user = authenticationRepo.findByuniversityEmail(verificationDTO.getEmail());
        if(user != null){
            if(!user.isEmailVerified()){
                if(user.getExpirationAt().isBefore(LocalDateTime.now())){
                  throw new VerificationCodeExpired("Verification Code expired");
                }
                if(user.getVerificationCode() == verificationDTO.getVerificationCode()){
                    user.setEnabled(true);
                    user.setEmailVerified(true);
                    user.setAccountRemarks("");
                    user.setVerificationCode(0);
                    if(user.getAccountStatus() == AccountStatus.Disabled) user.setAccountStatus(AccountStatus.Active);
                    user.setExpirationAt(null);
                    authenticationRepo.save(user);
                }else{
                    throw new VerificationCodeNotMatched("Verification code doesn't match");
                }
            }else{
                throw new AccountVerified("You are already verified");
            }
        }else{
            throw new AccountNotFound("user doesn't exists");
        }
    }

    public User login(loginDTO loginDto, HttpServletResponse response) {
        if(!util.isValidEmail(loginDto.getEmail())){
            throw new RuntimeException("Entered email is not valid");
        }
        User user =  authenticationRepo.findByuniversityEmail(loginDto.getEmail());
        if(user == null){
          throw new RuntimeException("Account doesn't exist");
        }
        boolean isCorrect = passwordEncoder.matches(loginDto.getPassword(),user.getPassword());
        if(isCorrect) {
            String token = jwtService.generateToken(user);
            setJwtInCookies(token,response);
            return user;
         }else{
            throw new RuntimeException("Credentials are incorrect");

        }
  }
  public void resendVerificationCode(String email) throws MessagingException,AccountNotFound {
        if(!util.isValidEmail(email)){
            throw new RuntimeException("Email is not valid");
        }
        User user = authenticationRepo.findByuniversityEmail(email);
        if(user == null){
            throw new AccountNotFound("user not found");
        }
        else if(user.isEmailVerified()){
            throw new RuntimeException("Account is already verified");
        }
        user.setVerificationCode(generateVerificationCode());
        user.setExpirationAt(LocalDateTime.now().plusMinutes(15));
        sendVerificationCode(user.getUniversityEmail(),user.getVerificationCode());
        authenticationRepo.save(user);
  }
   public userdetails getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()){

            if((authentication.getPrincipal() instanceof String)){
                return null;
            }
          return (userdetails) authentication.getPrincipal();
 }
        return null;
    }
    private void setJwtInCookies(String token, HttpServletResponse response){
        ResponseCookie cookie = ResponseCookie.from("jwt",token)
                .secure(false)
                .httpOnly(true)
                .path("/")
                .domain("localhost")
                .maxAge(3600)
                .build();
                response.setHeader(HttpHeaders.SET_COOKIE,cookie.toString());
    }

    public void logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt")
                .secure(false)
                .value("")
                .httpOnly(true)
                .maxAge(0)
                .path("/")
                .domain("localhost")
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE,cookie.toString());
    }
}
