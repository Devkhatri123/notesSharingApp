package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.DTO.RemarkRequest;
import com.notesSharingApp.notesSharingApp.DTO.userDTOWithoutNotes;
import com.notesSharingApp.notesSharingApp.Exception.Account.AccountIsBlocked;
import com.notesSharingApp.notesSharingApp.Exception.Account.AccountNotFound;
import com.notesSharingApp.notesSharingApp.Exception.Account.AccountVerified;
import com.notesSharingApp.notesSharingApp.Util.util;
import com.notesSharingApp.notesSharingApp.model.AccountStatus;
import com.notesSharingApp.notesSharingApp.model.TempUser;
import com.notesSharingApp.notesSharingApp.model.User;
import com.notesSharingApp.notesSharingApp.repository.ProfileRepo;
import com.notesSharingApp.notesSharingApp.repository.TempUserRepo;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProfileService {
    @Autowired
    private TempUserRepo tempUserRepo;
    @Autowired
    private ProfileRepo profileRepo;
    @Autowired
    private TempUserService tempUserService;
    @Autowired
    private EmailService emailService;


    // Sending user's info update request in tempUser table to let the admins
    // to-check whether the information in update request is appropriate, or not
    // if update information is valid, then tempUser row data will be copied in the main user table

     @Transactional
     public void updateUser(String userId, TempUser user) throws RuntimeException{

        // Fetching the user data from primary user table
        Optional<User> dbUser = profileRepo.findById(userId);
        if(dbUser.isPresent()){
            User u = dbUser.get();

            // save user info update request in tempUser table
            tempUserService.save(user);

            // Disabling the user account temporarily
            u.setAccountRemarks("Update Info Request Pending Review. Your account is disabled temporarily, it will be enabled once a decision is made by admin");
            u.setAccountStatus(AccountStatus.Disabled);
            profileRepo.save(u);
        }else {
            throw new AccountNotFound("No user Found");
        }
    }

    // Get update info approval pending request. Only manager/admin can access this
    public List<TempUser> getApprovalPendingUsersInfo(Integer pageNumber, Integer limit) {
        return tempUserRepo.getAllPendingProfiles(PageRequest.of(pageNumber,limit));
    }

    public void rejectUpdateInfoRequest(String userId, RemarkRequest remarkRequest) {
        Optional<TempUser> u = tempUserRepo.findById(userId);
        Optional<User> u2 = profileRepo.findById(userId);
        if(u.isPresent()){
            TempUser tempUser = u.get();
            tempUser.setRemarks(remarkRequest.getMessage());
            tempUser.setAccountStatus(AccountStatus.Declined);
            tempUserRepo.save(tempUser);
            if(u2.isPresent()){
                User primaryUser = u2.get();
                primaryUser.setAccountStatus(AccountStatus.Active);
                primaryUser.setAccountRemarks("");
                profileRepo.save(primaryUser);
            }
        }
    }

    public void approveChanges(String userId) throws MessagingException {
        Optional<TempUser> u = tempUserRepo.findById(userId);
        Optional<User> u2 = profileRepo.findById(userId);
        if (u2.isPresent() && u.isPresent()) {
            TempUser tempUser = u.get();
            User ActualUser = u2.get();

            ActualUser.setUsername(tempUser.getUsername());
            ActualUser.setSemester(tempUser.getSemester());
            ActualUser.setGender(tempUser.getGender());
            ActualUser.setDepartment(tempUser.getDepartment());
            ActualUser.setUniversityEmail(tempUser.getUniversityEmail());

            ActualUser.setAccountRemarks("Update info request has been approved by admin. Please verify Your email through otp. Otp has been send to you");
            ActualUser.setVerificationCode(util.generateVerificationCode());
            ActualUser.setEmailVerified(false);

            ActualUser.setExpirationAt(LocalDateTime.now().plusMinutes(15));
            emailService.sendVerificationCode(ActualUser.getUniversityEmail(),ActualUser.getVerificationCode());

            tempUser.setAccountStatus(AccountStatus.Approved);
            tempUser.setRemarks("Your Update Info Request has been Approved, changes have been applied");
            tempUserRepo.save(tempUser);
            profileRepo.save(ActualUser);

            System.out.println("Operation completed...");
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
    public void blockUser(String userId) {
        Optional<User> optionalUser = profileRepo.findById(userId);
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            user.setAccountStatus(AccountStatus.Blocked);
            user.setAccountRemarks("Your account is blocked");
            profileRepo.save(user);
        }
    }

    public List<userDTOWithoutNotes> getProfiles(String query, Integer pageNumber, Integer limit) {
        if(!query.isEmpty()) {
            List<User> profiles = profileRepo.searchByFullnameAndUniversityEmail(query, PageRequest.of(pageNumber, limit));
            return profiles.stream().map(util::convertUserModelToDTO).toList();
        }
        return null;
    }

    public void unBlockUser(String userId) {
         User user = profileRepo.findById(userId).orElseThrow(() -> new AccountNotFound("Account not found"));
         if(!user.isEmailVerified()){
             user.setAccountRemarks("Your account is disabled until you verify your email address.");
             user.setAccountStatus(AccountStatus.Disabled);
         }else {
             user.setAccountRemarks("");
             user.setAccountStatus(AccountStatus.Active);
         }
        profileRepo.save(user);
    }
    public User getuser(String userId){
        return profileRepo.findById(userId).orElseThrow(()-> new AccountNotFound("Account not found"));
    }
    public void saveUser(User user){
        profileRepo.save(user);
    }
    public long getPendingUpdatesProfiles(){
     return tempUserRepo.countByaccountStatus(AccountStatus.Pending);
    }

    public userDTOWithoutNotes getUserProfile(String userId) {
        Optional<User> OptionalUser = profileRepo.findById(userId);
        if(OptionalUser.isPresent())
        return util.convertUserModelToDTO(OptionalUser.get());
        else throw new AccountNotFound("profile not found");
    }
    public boolean getUserByUniversityEmail(String email){
       return profileRepo.existsByUniversityEmail(email);
    }

    public void activateProfile(String userId) {
         User user = getuser(userId);
         if(user.getAccountStatus() == AccountStatus.Blocked){
             throw new AccountIsBlocked("Account is blocked");
         }
         if(user.getAccountStatus() == AccountStatus.Disabled){
             if(!user.isEmailVerified()){
                 user.setEmailVerified(true);
                 user.setVerificationCode(0);
                 user.setExpirationAt(null);
             }
             user.setAccountRemarks("");
             user.setAccountStatus(AccountStatus.Active);
             profileRepo.save(user);
             return;
         }
        if(user.getAccountStatus() == AccountStatus.Active) throw new AccountVerified("Account is already active");
    }
}
