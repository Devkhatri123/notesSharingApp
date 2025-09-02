package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.DTO.RegisterDTO;
import com.notesSharingApp.notesSharingApp.DTO.ResetPasswordDTO;
import com.notesSharingApp.notesSharingApp.DTO.LoginDTO;
import com.notesSharingApp.notesSharingApp.DTO.VerificationDTO;
import com.notesSharingApp.notesSharingApp.Enum.AccountStatus;
import com.notesSharingApp.notesSharingApp.Enum.Role;
import com.notesSharingApp.notesSharingApp.Exception.*;
import com.notesSharingApp.notesSharingApp.Exception.Account.*;
import com.notesSharingApp.notesSharingApp.Util.util;
import com.notesSharingApp.notesSharingApp.model.*;
import com.notesSharingApp.notesSharingApp.repository.UserRepo;
import com.notesSharingApp.notesSharingApp.repository.ResetTokenRepo;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthenticationService {
    private final String[] departments = {"CS","CE"};
    @Autowired
    private EmailService emailService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ResetTokenRepo resetTokenRepo;
    @Autowired
    private UserRepo userRepo;


    @Transactional
    public void register(RegisterDTO registerDTO) throws MessagingException,UsernameAlreadyTaken,InvalidDepartment,EmailNotValid,EmailAlreadyInUse,CharacterLimitExceeded,InvalidSemesterSelected,InvalidGenderSelected {
        if(!util.isValidEmail(registerDTO.getUniversityEmail())){
            throw new EmailNotValid("University email is not valid");
        }else if(userRepo.existsByUsername(registerDTO.getUsername())){
            throw new UsernameAlreadyTaken("Username is already taken");
        }else if (userRepo.findByuniversityEmail(registerDTO.getUniversityEmail()) != null){
            throw new EmailAlreadyInUse("Email is already in use");
        }else if(!Arrays.stream(departments).anyMatch(registerDTO.getDepartment()::equals)){
            throw new InvalidDepartment(registerDTO.getDepartment() +" department is not available right now");
        }else if(registerDTO.getUsername().length() > 35){
            throw new CharacterLimitExceeded("Username should be of 35 characters");
        } else if(registerDTO.getSemester() <= 0 || registerDTO.getSemester() > 8){
            throw new InvalidSemesterSelected("Semester can be only between 1-8");
        } else if (!registerDTO.getGender().equals("Male") && !registerDTO.getGender().equals("Female") && !registerDTO.getGender().equals("Other")){
             throw new InvalidGenderSelected("Invalid gender selected");
        }

        User user = modelMapper.map(registerDTO,User.class);
        // set UUId,encode password,role("student") and disable account until verification is done
        user.setId(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAccountStatus(AccountStatus.Active);
        user.setEmailVerified(false);
        user.setRoles(Set.of(Role.STUDENT));
        user.setAccountRemarks("Your email is not verified");

        // Verification Code Logic
        user.setVerificationCode(util.generateVerificationCode());
        user.setExpirationAt(LocalDateTime.now().plusMinutes(15));
        User savedUser = userRepo.save(user);
        if(userRepo.findById(savedUser.getId()).isPresent()) {
            sendVerificationCode(user.getUniversityEmail(), user.getVerificationCode());
        }else{
            throw new RuntimeException("Failed in creating account.Please try again");
        }
    }
    private void sendVerificationCode(String to,int verificationCode) throws MessagingException {
        emailService.sendVerificationCode(to,verificationCode);
    }

    public void verify(VerificationDTO verificationDTO) throws VerificationCodeExpired,EmailNotValid,AccountNotFound,AccountVerified,VerificationCodeNotMatched {
        if(verificationDTO.getEmail() == null){
            throw new EmailNotValid("Email not provided");
        }
        User user = userRepo.findByuniversityEmail(verificationDTO.getEmail());
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
                    userRepo.save(user);
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

    public User login(LoginDTO loginDto, HttpServletResponse response) throws EmailNotValid,AccountNotFound,BadCredentialsException {
        if(!util.isValidEmail(loginDto.getEmail())){
            throw new EmailNotValid("Entered email is not valid");
        }
        User user =  userRepo.findByuniversityEmail(loginDto.getEmail());
        if(user == null){
          throw new AccountNotFound("Account doesn't exist");
        }
        boolean isCorrect = passwordEncoder.matches(loginDto.getPassword(),user.getPassword());
        if(isCorrect) {
            String token = jwtService.generateToken(user);
            setJwtInCookies(token,response);
            return user;
         }else{
            throw new BadCredentialsException("Credentials are incorrect");

        }
  }
  @Transactional
  public void resendVerificationCode(String email) throws MessagingException,AccountNotFound,EmailNotValid,AccountVerified {
        if(!util.isValidEmail(email)){
            throw new EmailNotValid("Email is not valid");
        }
        User user = userRepo.findByuniversityEmail(email);
        if(user == null){
            throw new AccountNotFound("user not found");
        } else if(user.isEmailVerified()){
            throw new AccountVerified("Account is already verified");
        }
        user.setVerificationCode(util.generateVerificationCode());
        user.setExpirationAt(LocalDateTime.now().plusMinutes(15));
        sendVerificationCode(user.getUniversityEmail(),user.getVerificationCode());
        userRepo.save(user);
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
                .secure(true)
                .sameSite("None")
                .httpOnly(true)
                .path("/")
                .maxAge(3600)
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE,cookie.toString());
    }

    public void logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt")
                .secure(true)
                .sameSite("None")
                .value("")
                .httpOnly(true)
                .maxAge(0)
                .path("/")
               // .domain("localhost")
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE,cookie.toString());
    }

    // Generating reset password link and sending it to user mail
    @Transactional
    public void createResetPasswordToken(String email) throws AccountNotFound, MessagingException {
      Optional<User> userOptional = userRepo.findOneByUniversityEmail(email);
      if(userOptional.isPresent()) {
          // Deleting already present reset token of user
          resetTokenRepo.deleteTokenByUserId(userOptional.get().getId());
          // creating new reset token
          ResetToken resetToken = new ResetToken();
          String token = UUID.randomUUID().toString();
          resetToken.setResetToken(passwordEncoder.encode(token));
          resetToken.setUser(userOptional.get());
          resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
          resetTokenRepo.save(resetToken);
          emailService.sendForgotPasswordLink(token,email);
          return;
      }
      throw new AccountNotFound("user not found");
    }

    // Resest password
    @Transactional
    public void resetPassword(String token, String email, ResetPasswordDTO resetPasswordDTO) throws TokenNotFound,TokenExpired,InvalidToken,AccountNotFound {
      Optional<User> userOptional = userRepo.findUserByUniversityEmail(email);
      if(userOptional.isPresent()){
          User user = userOptional.get();
          ResetToken resetToken = resetTokenRepo.findOneByUser(user);
          if(resetToken == null){
            throw new TokenNotFound("Invalid token or expired token");
          }
          if(resetToken.isExpired()){
              throw new TokenExpired("Token expired. Please request new token.");
          }
          if(!passwordEncoder.matches(token,resetToken.getResetToken())){
              throw new InvalidToken("Invalid token");
          }
         user.setPassword(passwordEncoder.encode(resetPasswordDTO.getPassword()));
         userRepo.save(user);
         resetTokenRepo.deleteTokenByUserId(user.getId());
         return;
      }
      throw new AccountNotFound("user not found");
    }
}
