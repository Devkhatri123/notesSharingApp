package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.DTO.RemarkRequest;
import com.notesSharingApp.notesSharingApp.DTO.UserDTOWithoutNotes;
import com.notesSharingApp.notesSharingApp.Enum.Role;
import com.notesSharingApp.notesSharingApp.Exception.Account.*;
import com.notesSharingApp.notesSharingApp.Exception.CharacterLimitExceeded;
import com.notesSharingApp.notesSharingApp.Exception.DecisionAlreadyMade;
import com.notesSharingApp.notesSharingApp.Util.util;
import com.notesSharingApp.notesSharingApp.Enum.AccountStatus;
import com.notesSharingApp.notesSharingApp.model.TempUser;
import com.notesSharingApp.notesSharingApp.model.User;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import com.notesSharingApp.notesSharingApp.repository.UserRepo;
import com.notesSharingApp.notesSharingApp.repository.TempUserRepo;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
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
    private final TempUserRepo tempUserRepo;
    private final UserRepo userRepo;
    private final TempUserService tempUserService;
    private final EmailService emailService;

    @Autowired
    public ProfileService(TempUserRepo tempUserRepo, UserRepo userRepo, TempUserService tempUserService, EmailService emailService, ModelMapper modelMapper){
        this.tempUserService = tempUserService;
        this.tempUserRepo = tempUserRepo;
        this.userRepo = userRepo;
        this.emailService = emailService;
    }

    // Sending user's info update request in tempUser table to let the admins
    // to-check whether the information in update request is appropriate, or not
    // if update information is valid, then tempUser row data will be copied in the main user table

     @Transactional
     public void updateUser(String userId, TempUser user) throws EmailAlreadyInUse, UsernameAlreadyTaken,CharacterLimitExceeded{
        // Fetching the user data from primary user table
        Optional<User> dbUser = userRepo.findById(userId);
        if(dbUser.isPresent()){
            User u = dbUser.get();
            // save user info update request in tempUser table
            tempUserService.save(user);
            if(u.getVerificationCode() != 0){
                u.setVerificationCode(0);
                u.setExpirationAt(null);
                u.setEmailVerified(true);
            }
            // Disabling the user account temporarily
            u.setAccountRemarks("Update Info Request Pending Review. Your account is disabled temporarily, it will be enabled once a decision is made by admin");
            u.setAccountStatus(AccountStatus.Disabled);
            userRepo.save(u);
        }else {
            throw new AccountNotFound("No user Found");
        }
    }

    // Get update info approval pending request. Only manager/admin can access this
    public List<TempUser> getApprovalPendingUsersInfo(Integer pageNumber, Integer limit) {
        List<TempUser> profiles = tempUserRepo.getAllPendingProfiles(PageRequest.of(pageNumber,limit));
        userdetails authenticatedUser = util.getAuthenticatedUser();
        if(!authenticatedUser.getUser().getRoles().contains(Role.MANAGER)){
          profiles = profiles.stream().filter(profile -> {
                // Returning only admin's department profiles
                return profile.getDepartment().equals(authenticatedUser.getUser().getDepartment());
            }).toList();
        }
        return profiles;
    }

    // If anything goes wrong while updating tempUser or actual user data, then rollback the transaction
    // to make sure the database in consistent
    @Transactional
    public void rejectUpdateInfoRequest(String userId, RemarkRequest remarkRequest) throws DecisionAlreadyMade,AccountNotFound,CharacterLimitExceeded {
        if(remarkRequest.getMessage().length() > 512){
            throw new CharacterLimitExceeded("Remark message limit is 512 characters");
        }
        Optional<TempUser> optionalTempUser = tempUserRepo.findById(userId);
        Optional<User> u2 = userRepo.findById(userId);
        if(optionalTempUser.isPresent()){
            TempUser tempUser = optionalTempUser.get();
            if(tempUser.getAccountStatus() == AccountStatus.Approved){
              throw new DecisionAlreadyMade("This request has been already approved");
            }else if (tempUser.getAccountStatus() == AccountStatus.Declined){
                throw new DecisionAlreadyMade("This request has been already declined");
            }
            // Reject user info update request
            tempUserService.rejectTempUserRequest(tempUser,remarkRequest);
            if(u2.isPresent()){
                // Set account status of user to active and clear account remarks
                User primaryUser = u2.get();
                primaryUser.setAccountStatus(AccountStatus.Active);
                primaryUser.setAccountRemarks("");
                userRepo.save(primaryUser);
            }
        } else throw new AccountNotFound("Decision is already made for this request");
    }

    // If anything goes wrong in updating tempUser or actual user data, then rollback the transaction
    // to make sure the database in consistent
    @Transactional
    public User approveChanges(String userId) throws MessagingException,DecisionAlreadyMade,EmailAlreadyInUse,UsernameAlreadyTaken {
        Optional<TempUser> optionalTempUser = tempUserRepo.findById(userId);
        Optional<User> userOptional = userRepo.findById(userId);
        if (optionalTempUser.isPresent()) {
            TempUser tempUser = optionalTempUser.get();
            User ActualUser = userOptional.get();

                // Checking whether had request already been processed
                if (tempUser.getAccountStatus() == AccountStatus.Approved) {
                    throw new DecisionAlreadyMade("This request has been already approved");
                } else if (tempUser.getAccountStatus() == AccountStatus.Declined) {
                    throw new DecisionAlreadyMade("This request has been already declined");
                }

                // Setting new values of update request in the user account
                ActualUser.setUsername(tempUser.getUsername());
                ActualUser.setSemester(tempUser.getSemester());
                ActualUser.setGender(tempUser.getGender());
                ActualUser.setDepartment(tempUser.getDepartment());
                ActualUser.setUniversityEmail(tempUser.getUniversityEmail());

                ActualUser.setAccountRemarks("Update info request has been approved by admin. Please verify Your email through otp. Otp has been sent to you");
                ActualUser.setVerificationCode(util.generateVerificationCode());
                ActualUser.setEmailVerified(false);
                ActualUser.setExpirationAt(LocalDateTime.now().plusMinutes(15));

                tempUserService.approveRequest(tempUser);
                userRepo.save(ActualUser);
                return ActualUser;
     } else throw new DecisionAlreadyMade("Decision is already made for this request");
    }

   // sending an email to user of email verification to make sure the new email is valid
   // after the profile update request approval from admin
    public void notifyUser(User user) throws MessagingException {
       emailService.sendVerificationCode(user.getUniversityEmail(),user.getVerificationCode());
    }
    // Get the userInfo update request
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

    // Block user
    public void blockUser(String userId,String blockReason) throws AccountNotFound,CharacterLimitExceeded {
        if(blockReason.length() > 512) throw new CharacterLimitExceeded("Reason should be of 512 characters");
        Optional<User> optionalUser = userRepo.findById(userId);
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            user.setAccountStatus(AccountStatus.Blocked);
            user.setAccountRemarks(blockReason);
            userRepo.save(user);
            return;
        }
        throw new AccountNotFound("User not found");
    }

    // Get user profiles from search (Admin, Manager level access)
    public List<UserDTOWithoutNotes> getProfiles(String query, Integer pageNumber, Integer limit) {
        if(!query.isEmpty()) {
            userdetails authenticatedUser = util.getAuthenticatedUser();
            List<User> profiles = userRepo.searchByFullnameAndUniversityEmail(query, PageRequest.of(pageNumber, limit));
            if(!authenticatedUser.getUser().getRoles().contains(Role.MANAGER)){
                profiles = profiles.stream().filter(user -> {
                    return !user.getRoles().contains(Role.ADMIN) && !user.getRoles().contains(Role.MANAGER)
                            &&
                            user.getDepartment().equals(authenticatedUser.getUser().getDepartment());
                }).toList();
            }
            return profiles.stream().map(util::convertUserModelToDTO).toList();
        }
        return null;
    }

    public void unBlockUser(String userId) throws AccountNotFound {
         User user = userRepo.findById(userId).orElseThrow(() -> new AccountNotFound("Account not found"));
         if(!user.isEmailVerified()){
             user.setAccountRemarks("Your account is disabled until you verify your email address.");
             user.setAccountStatus(AccountStatus.Disabled);
         }else {
             user.setAccountRemarks("");
             user.setAccountStatus(AccountStatus.Active);
         }
        userRepo.save(user);
    }

    public User getuser(String userId) throws AccountNotFound{
        return userRepo.findById(userId).orElseThrow(()-> new AccountNotFound("Account not found"));
    }
    public void saveUser(User user){
        userRepo.save(user);
    }
    public long getPendingUpdatesProfiles(){
     return tempUserRepo.countByaccountStatus(AccountStatus.Pending);
    }

    public UserDTOWithoutNotes getUserProfile(String userId) throws AccountNotFound {
        Optional<User> OptionalUser = userRepo.findById(userId);
        if(OptionalUser.isPresent())
        return util.convertUserModelToDTO(OptionalUser.get());
        else throw new AccountNotFound("profile not found");
    }
    public boolean getUserByUniversityEmail(String email){
       return userRepo.existsByUniversityEmail(email);
    }
    public boolean getUserByUsername(String username){
       return userRepo.existsByUsername(username);
    }
    // Activate user profile if account Status is disabled
    public void activateProfile(String userId) throws AccountIsBlocked,AccountVerified {
         User user = getuser(userId);
         if(user.getAccountStatus() == AccountStatus.Blocked){
             throw new AccountIsBlocked("Account is blocked");
         }
         else if(user.getAccountStatus() == AccountStatus.Disabled){
             if(!user.isEmailVerified()){
                 List<Role> roles = user.getRoles().stream().toList();
                 if(roles.contains(Role.ADMIN) || roles.contains(Role.MANAGER)){
                  user.setAccountRemarks("Your email is not verified, verify your email to upload notes and perform admin operations");
                 }else if(roles.contains(Role.STUDENT)){
                     user.setAccountRemarks("Your email is not verified, verify your email to upload notes.");
                 }
             }else user.setAccountRemarks("");
             user.setAccountStatus(AccountStatus.Active);
             userRepo.save(user);
             return;
         }
        if(user.getAccountStatus() == AccountStatus.Active) throw new AccountVerified("Account is already active");
    }
}
