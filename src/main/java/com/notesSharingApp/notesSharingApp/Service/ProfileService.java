package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.DTO.RemarkRequest;
import com.notesSharingApp.notesSharingApp.DTO.userDTOWithoutNotes;
import com.notesSharingApp.notesSharingApp.Exception.AccountNotFound;
import com.notesSharingApp.notesSharingApp.Exception.EmailAlreadyInUse;
import com.notesSharingApp.notesSharingApp.Exception.EmailNotValid;
import com.notesSharingApp.notesSharingApp.Util.util;
import com.notesSharingApp.notesSharingApp.model.AccountStatus;
import com.notesSharingApp.notesSharingApp.model.TempUser;
import com.notesSharingApp.notesSharingApp.model.User;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import com.notesSharingApp.notesSharingApp.repository.ProfileRepo;
import com.notesSharingApp.notesSharingApp.repository.TempUserRepo;
import jakarta.mail.MessageRemovedException;
import jakarta.mail.MessagingException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    private ModelMapper modelMapper;
    @Autowired
    private EmailService emailService;

    // Sending user's info update request in tempUser table to let the admins
    // to-check whether the information in update request is appropriate, or not
    // if update information is valid, then tempUser row data will be copied in the main user table
    public void updateUser(String userId, TempUser user) throws RuntimeException{

        if(!util.isValidEmail(user.getUniversityEmail())){
            throw new EmailNotValid("Email is not valid");
        }
         userdetails userdetails = util.getAuthenticatedUser();
        if(!userdetails.getUser().getUniversityEmail().equalsIgnoreCase(user.getUniversityEmail())){
            if(profileRepo.existsByUniversityEmail(user.getUniversityEmail())){
                throw new EmailAlreadyInUse("Email is already taken");
            }
        }

        // Fetching the user data from primary user table
        Optional<User> dbUser = profileRepo.findById(userId);
        if(dbUser.isPresent()){
            User u = dbUser.get();
            user.setRemarks("Update Request Pending Review");
            user.setAccountStatus(AccountStatus.Pending);
            user.setRequestAt(LocalDate.now());
            tempUserRepo.save(user);

            // Disabling the user account temporarily
            u.setAccountRemarks("Update Info Request Pending Review. Your account is disabled temporarily, it will be enabled once a decision is made by admin");
            u.setAccountStatus(AccountStatus.Disabled);
            profileRepo.save(u);
        }else {
            throw new AccountNotFound("No user Found");
        }
    }

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

            ActualUser.setFullname(tempUser.getFullname());
            ActualUser.setSemester(tempUser.getSemester());
            ActualUser.setGender(tempUser.getGender());
            ActualUser.setDepartment(tempUser.getDepartment());
            ActualUser.setContact(tempUser.getContact());
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

        profileRepo.UnBlockUser(userId);
    }
    public User getuser(String userId){
        return profileRepo.findById(userId).get();
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
}
