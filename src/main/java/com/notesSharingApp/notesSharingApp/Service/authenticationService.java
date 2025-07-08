package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.DTO.loginDTO;
import com.notesSharingApp.notesSharingApp.DTO.verificationDTO;
import com.notesSharingApp.notesSharingApp.Exception.AccountIsDisabled;
import com.notesSharingApp.notesSharingApp.Exception.AccountNotFound;
import com.notesSharingApp.notesSharingApp.Exception.VerificationCodeExpired;
import com.notesSharingApp.notesSharingApp.model.user;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.notesSharingApp.notesSharingApp.repository.authenticationRepo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class authenticationService {
    private final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private final String[] departments = {"CS","CE"};
    @Autowired
    private authenticationRepo authenticationRepo;
    @Autowired
    private emailService emailService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private userdetailsService userdetailsService;

    public void register(user user) throws MessagingException,RuntimeException {
        if(!isValidEmail(user.getEmail())){
            throw new RuntimeException("Email is not valid");
        }else if(!Arrays.stream(departments).anyMatch(user.getDepartment()::equals)){
            throw new RuntimeException(user.getDepartment() +" department is not available right now");
        }else if(authenticationRepo.findByContact(user.getContact()) != null){
            throw new RuntimeException("Phone number is already taken");
        }else if (authenticationRepo.findByemail(user.getEmail()) != null){
            throw new RuntimeException("Email is already taken");
        }

         // set UUId,encode password,role("student") and disable account until verification is done
        user.setId(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);
        user.setRole("STUDENT");

        // Verification Code Logic
        user.setVerificationCode(generateVerificationCode());
        user.setExpirationAt(LocalDateTime.now().plusMinutes(15));
        sendVerificationCode(user.getEmail(),user.getVerificationCode());

        authenticationRepo.save(user);
    }
    // Generate Verification Code
    private int generateVerificationCode(){
        Random random = new Random();
        return random.nextInt(1000,9999);
    }
    private void sendVerificationCode(String to,int verificationCode) throws MessagingException {
        emailService.sendVerificationCode(to,verificationCode);
    }

    public void verify(verificationDTO verificationDTO) throws VerificationCodeExpired,RuntimeException {
        user user = authenticationRepo.findByemail(verificationDTO.getEmail());
        if(user != null){
            if(!user.isEnabled()){
                if(user.getExpirationAt().isBefore(LocalDateTime.now())){
                  throw new VerificationCodeExpired("Verification Code expired");
                }
                if(user.getVerificationCode() == verificationDTO.getVerificationCode()){
                    user.setEnabled(true);
                    user.setVerificationCode(0);
                    user.setExpirationAt(null);

                    authenticationRepo.save(user);
                }
            }else{
                throw new RuntimeException("user is already verified");
            }
        }else{
            throw new RuntimeException("user doesn't exists");
        }
    }

    public user login(loginDTO loginDto) {
        user user =  authenticationRepo.findByemail(loginDto.getEmail());
        if(!isValidEmail(loginDto.getEmail())){
            throw new RuntimeException("Entered email is not valid");
        }
        if(user == null){
          throw new RuntimeException("No User found");
        }
        boolean isCorrect = passwordEncoder.matches(loginDto.getPassword(),user.getPassword());
        if(isCorrect) {
            if(user.isEnabled()) return user;
            else throw new AccountIsDisabled("Your email is not verified.Please verify your email ");
        }else{
            throw new RuntimeException("Credentials are incorrect");

        }
  }
  public void resendVerificationCode(String email) throws MessagingException,AccountNotFound {
        user user = authenticationRepo.findByemail(email);
        if(user == null){
            throw new AccountNotFound("user not found");
        }
        if(user.isEnabled()){
            throw new RuntimeException("Account is already verified");
        }
        user.setVerificationCode(generateVerificationCode());
        user.setExpirationAt(LocalDateTime.now().plusMinutes(15));
        sendVerificationCode(user.getEmail(),user.getVerificationCode());
        authenticationRepo.save(user);
  }


    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
