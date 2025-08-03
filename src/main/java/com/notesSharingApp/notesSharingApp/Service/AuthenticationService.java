package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.DTO.loginDTO;
import com.notesSharingApp.notesSharingApp.DTO.userDTOWithoutNotes;
import com.notesSharingApp.notesSharingApp.DTO.verificationDTO;
import com.notesSharingApp.notesSharingApp.Exception.AccountVerified;
import com.notesSharingApp.notesSharingApp.Exception.AccountNotFound;
import com.notesSharingApp.notesSharingApp.Exception.VerificationCodeExpired;
import com.notesSharingApp.notesSharingApp.model.Status;
import com.notesSharingApp.notesSharingApp.model.user;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import com.notesSharingApp.notesSharingApp.repository.UserUpdateRequest;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.notesSharingApp.notesSharingApp.repository.authenticationRepo;
import com.notesSharingApp.notesSharingApp.model.TempUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AuthenticationService {
    private final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private final String UNIVERSITY_MAIL_REGEX = "^csd\\d{2}(?:0[1-9]|1[0-2])\\d{2}+@dsu.edu.pk$";
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
    @Autowired
    private jwtService jwtService;
    @Autowired
    private UserUpdateRequest userUpdateRequest;
    @Autowired
    private ModelMapper modelMapper;


    public void register(user user) throws MessagingException,RuntimeException {
        if(!isValidEmail(user.getUniversityEmail())){
            throw new RuntimeException("University email is not valid");
        }else if (authenticationRepo.findByuniversityEmail(user.getUniversityEmail()) != null){
            throw new RuntimeException("Email is already taken");
        }else if(!Arrays.stream(departments).anyMatch(user.getDepartment()::equals)){
            throw new RuntimeException(user.getDepartment() +" department is not available right now");
        }else if(authenticationRepo.findBycontact(user.getContact()) != null){
            throw new RuntimeException("Phone number is already taken");
        }


        // set UUId,encode password,role("student") and disable account until verification is done
        user.setId(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setRole("STUDENT");

        // Verification Code Logic
        user.setVerificationCode(generateVerificationCode());
        user.setExpirationAt(LocalDateTime.now().plusMinutes(15));
        user.setEmailVerified(false);
        user savedUser = authenticationRepo.save(user);
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

    public void verify(verificationDTO verificationDTO) throws VerificationCodeExpired,RuntimeException {
        if(verificationDTO.getEmail() == null){
            throw new RuntimeException("Email not provided");
        }
        user user = authenticationRepo.findByuniversityEmail(verificationDTO.getEmail());
        if(user != null){
            if(!user.isEmailVerified()){
                if(user.getExpirationAt().isBefore(LocalDateTime.now())){
                  throw new VerificationCodeExpired("Verification Code expired");
                }
                if(user.getVerificationCode() == verificationDTO.getVerificationCode()){
                    user.setEnabled(true);
                    user.setEmailVerified(true);
                    user.setVerificationCode(0);
                    user.setExpirationAt(null);
                    authenticationRepo.save(user);
                }else{
                    throw new RuntimeException("Verification code doesn't match");
                }
            }else{
                throw new AccountVerified("user is already verified");
            }
        }else{
            throw new RuntimeException("user doesn't exists");
        }
    }

    public user login(loginDTO loginDto,HttpServletResponse response) {
        if(!isValidEmail(loginDto.getEmail())){
            throw new RuntimeException("Entered email is not valid");
        }
        user user =  authenticationRepo.findByuniversityEmail(loginDto.getEmail());
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
        if(!isValidEmail(email)){
            throw new RuntimeException("Email is not valid");
        }
        user user = authenticationRepo.findByuniversityEmail(email);
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


    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(UNIVERSITY_MAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
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
    public userDTOWithoutNotes convertUserModelToDTO(user user){
        final userDTOWithoutNotes userDTOWithoutNotes = new userDTOWithoutNotes();

        userDTOWithoutNotes.setId(user.getId());
        userDTOWithoutNotes.setName(user.getFullname());
        userDTOWithoutNotes.setUniversityEmail(user.getUniversityEmail());
        userDTOWithoutNotes.setSemester(user.getSemester());
        userDTOWithoutNotes.setDepartment(user.getDepartment());
        userDTOWithoutNotes.setEnabled(user.isEnabled());
        userDTOWithoutNotes.setRole(user.getRole());
        userDTOWithoutNotes.setPhone(user.getContact());
        userDTOWithoutNotes.setGender(user.getGender());
        userDTOWithoutNotes.setUniversityEmail(user.getUniversityEmail());
        userDTOWithoutNotes.setEmailVerified(user.isEmailVerified());

        return userDTOWithoutNotes;
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
    public void updateUser(String userId,TempUser user) throws RuntimeException{

        if(!isValidEmail(user.getUniversityEmail())){
             throw new RuntimeException("Email is not valid");
        }
      Optional<user> dbUser = authenticationRepo.findById(userId);
      if(dbUser.isPresent()){
          user.setRemarks("Pending Review");
          user.setAccountStatus(Status.Pending);
          user.setRequestAt(LocalDate.now());
          userUpdateRequest.save(user);
          user u = dbUser.get();
          u.setEnabled(false);
          authenticationRepo.save(u);
      }else {
          throw new RuntimeException("No user in Db");
      }
    }
    public TempUser ConvertToUserUpdateRequest(userDTOWithoutNotes user){
       return modelMapper.map(user, TempUser.class);
    }


    public List<TempUser> getApprovalPendingUsersInfo(Integer pageNumber,Integer limit) {
        return userUpdateRequest.findAll(PageRequest.of(pageNumber,limit)).getContent();
    }
}
