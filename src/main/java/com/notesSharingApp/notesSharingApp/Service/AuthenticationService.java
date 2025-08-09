package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.DTO.RemarkRequest;
import com.notesSharingApp.notesSharingApp.DTO.loginDTO;
import com.notesSharingApp.notesSharingApp.DTO.userDTOWithoutNotes;
import com.notesSharingApp.notesSharingApp.DTO.verificationDTO;
import com.notesSharingApp.notesSharingApp.Exception.AccountVerified;
import com.notesSharingApp.notesSharingApp.Exception.AccountNotFound;
import com.notesSharingApp.notesSharingApp.Exception.VerificationCodeExpired;
import com.notesSharingApp.notesSharingApp.model.*;
import com.notesSharingApp.notesSharingApp.repository.TempUserRepo;
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
import com.notesSharingApp.notesSharingApp.repository.AuthenticationRepo;

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
    private AuthenticationRepo authenticationRepo;
    @Autowired
    private EmailService emailService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private userdetailsService userdetailsService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private TempUserRepo tempUserRepo;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    NotesService notesService;


    public void register(User user) throws MessagingException,RuntimeException {
        if(!isValidEmail(user.getUniversityEmail())){
            throw new RuntimeException("University email is not valid");
        }else if (authenticationRepo.findByuniversityEmail(user.getUniversityEmail()) != null){
            throw new RuntimeException("Email is already in use");
        }else if(!Arrays.stream(departments).anyMatch(user.getDepartment()::equals)){
            throw new RuntimeException(user.getDepartment() +" department is not available right now");
        }else if(authenticationRepo.findBycontact(user.getContact()) != null){
            throw new RuntimeException("Phone number is already taken");
        }


        // set UUId,encode password,role("student") and disable account until verification is done
        user.setId(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAccountStatus(AccountStatus.Active);
        user.setEmailVerified(false);
        user.setRole("STUDENT");
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

    public void verify(verificationDTO verificationDTO) throws VerificationCodeExpired,RuntimeException {
        if(verificationDTO.getEmail() == null){
            throw new RuntimeException("Email not provided");
        }
        User user = authenticationRepo.findByuniversityEmail(verificationDTO.getEmail());
        if(user != null){
            if(!user.isEmailVerified()){
                if(user.getExpirationAt().isBefore(LocalDateTime.now())){
                  throw new VerificationCodeExpired("Verification Code expired");
                }
                if(user.getVerificationCode() == verificationDTO.getVerificationCode()){
                    user.setEmailVerified(true);
                    user.setAccountRemarks("");
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

    public User login(loginDTO loginDto, HttpServletResponse response) {
        if(!isValidEmail(loginDto.getEmail())){
            throw new RuntimeException("Entered email is not valid");
        }
        User user =  authenticationRepo.findByuniversityEmail(loginDto.getEmail());
        if(user == null){
          throw new RuntimeException("Account doesn't exist");
        }
        boolean isCorrect = passwordEncoder.matches(loginDto.getPassword(),user.getPassword());
        if(isCorrect) {
           // user.setMyNotes(notesService.getAllNote(user.getId()));
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
    public userDTOWithoutNotes convertUserModelToDTO(User user){
        final userDTOWithoutNotes userDTOWithoutNotes = new userDTOWithoutNotes();

        userDTOWithoutNotes.setId(user.getId());
        userDTOWithoutNotes.setName(user.getFullname());
        userDTOWithoutNotes.setUniversityEmail(user.getUniversityEmail());
        userDTOWithoutNotes.setSemester(user.getSemester());
        userDTOWithoutNotes.setDepartment(user.getDepartment());
        userDTOWithoutNotes.setEnabled(user.isEnabled());
        userDTOWithoutNotes.setAccountStatus(user.getAccountStatus().toString());
        userDTOWithoutNotes.setAccountRemarks(user.getAccountRemarks());
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
    // Sending user's info update request in tempUser table to let the admins
    // to-check whether the information in update request is appropriate, or not
    // if update information is valid, then tempUser row data will be copied in the main user table
    public void updateUser(String userId,TempUser user) throws RuntimeException{
       if(!isValidEmail(user.getUniversityEmail())){
             throw new RuntimeException("Email is not valid");
       }
       // Fetching the user data from primary user table
      Optional<User> dbUser = authenticationRepo.findById(userId);
      if(dbUser.isPresent()){
          User u = dbUser.get();
          user.setRemarks("Update Request Pending Review");
          user.setAccountStatus(AccountStatus.Pending);
          user.setRequestAt(LocalDate.now());
          tempUserRepo.save(user);

          // Disabling the user account temporarily
          u.setAccountRemarks("Update Info Request Pending Review. Your account is disabled temporarily, it will be enabled once a decision is made by admin");
          u.setAccountStatus(AccountStatus.Disabled);
          authenticationRepo.save(u);
      }else {
          throw new RuntimeException("No user Found");
      }
    }
    public TempUser ConvertToUserUpdateRequest(userDTOWithoutNotes user){
       return modelMapper.map(user, TempUser.class);
    }


    public List<TempUser> getApprovalPendingUsersInfo(Integer pageNumber,Integer limit) {
        return tempUserRepo.findAllByaccountStatus_(AccountStatus.Pending,PageRequest.of(pageNumber,limit));
    }

    public void rejectUpdateInfoRequest(String userId, RemarkRequest remarkRequest) {
       Optional<TempUser> u = tempUserRepo.findById(userId);
       Optional<User> u2 = authenticationRepo.findById(userId);
       if(u.isPresent()){
           TempUser tempUser = u.get();
           tempUser.setRemarks(remarkRequest.getMessage());
           tempUser.setAccountStatus(AccountStatus.Declined);
           tempUserRepo.save(tempUser);
           if(u2.isPresent()){
               User RealUser = u2.get();
               RealUser.setAccountStatus(AccountStatus.Active);
               RealUser.setAccountRemarks("");
               authenticationRepo.save(RealUser);
           }
       }
    }

    public void approveChanges(String userId) {
        Optional<TempUser> u = tempUserRepo.findById(userId);
        Optional<User> u2 = authenticationRepo.findById(userId);
        if (u2.isPresent() && u.isPresent()) {
            TempUser tempUser = u.get();
            User ActualUser = u2.get();

            ActualUser.setFullname(tempUser.getName());
            ActualUser.setSemester(tempUser.getSemester());
            ActualUser.setGender(tempUser.getGender());
            ActualUser.setDepartment(tempUser.getDepartment());
            ActualUser.setContact(tempUser.getPhone());
            ActualUser.setAccountStatus(AccountStatus.Active);
            ActualUser.setAccountRemarks("");

            tempUser.setAccountStatus(AccountStatus.Approved);
            tempUser.setRemarks("Your Update Request was Approved,changes has been applied");
            tempUserRepo.save(tempUser);
            authenticationRepo.save(ActualUser);
        }
    }
    public Map<String,String> getUserInfoUpdateRequestStatus(String userId){
       if(tempUserRepo.existsById(userId)) {
           TempUser user = tempUserRepo.findOneByid(userId);
           Map<String, String> statusMap = new HashMap<>();
           statusMap.put("remark", user.getRemarks());
           statusMap.put("status", user.getAccountStatus().toString());
           return statusMap;
       } else return null;
    }

    public void deleteUpdateInfoRequest(String userID) {
        if(tempUserRepo.existsById(userID)) {
            tempUserRepo.deleteById(userID);
        }
    }
}
