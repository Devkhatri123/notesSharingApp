package com.notesSharingApp.notesSharingApp.Service;


import com.notesSharingApp.notesSharingApp.DTO.RemarkRequest;
import com.notesSharingApp.notesSharingApp.Exception.Account.EmailAlreadyInUse;
import com.notesSharingApp.notesSharingApp.Exception.Account.EmailNotValid;
import com.notesSharingApp.notesSharingApp.Exception.Account.UsernameAlreadyTaken;
import com.notesSharingApp.notesSharingApp.Exception.CharacterLimitExceeded;
import com.notesSharingApp.notesSharingApp.Exception.DecisionAlreadyMade;
import com.notesSharingApp.notesSharingApp.Util.util;
import com.notesSharingApp.notesSharingApp.Enum.AccountStatus;
import com.notesSharingApp.notesSharingApp.model.TempUser;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import com.notesSharingApp.notesSharingApp.repository.TempUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TempUserService {
    @Autowired
    private TempUserRepo tempUserRepo;
    @Autowired
    @Lazy
    private ProfileService profileService;

    public boolean existsById(String id){
        return tempUserRepo.existsById(id);
    }
    public void deleteById(String id){
        tempUserRepo.deleteById(id);
    }
    public long countByAccountStatus(AccountStatus accountStatus){
        return tempUserRepo.countByaccountStatus(accountStatus);
    }
    public TempUser getTempUser(String id) throws DecisionAlreadyMade{
      Optional<TempUser> tempUser = tempUserRepo.findById(id);
        if(tempUser.isPresent()) {
            return tempUserRepo.findById(id).get();
        }
        throw new DecisionAlreadyMade("Decision is already made by admin for this request");
    }

    public void save(TempUser tempUser) throws EmailNotValid,EmailAlreadyInUse,UsernameAlreadyTaken,CharacterLimitExceeded{
        if(!util.isValidEmail(tempUser.getUniversityEmail())){
            throw new EmailNotValid("Email is not valid");
        }
        if(tempUser.getUsername().length() > 35){
            throw new CharacterLimitExceeded("Username should be of 35 characters");
        }
        userdetails userdetails = util.getAuthenticatedUser();
        if(!userdetails.getUser().getUsername().equalsIgnoreCase(tempUser.getUsername())){
          if(profileService.getUserByUsername(tempUser.getUsername())){
              throw new UsernameAlreadyTaken("Username is already taken");
          }
        } else if(!userdetails.getUser().getUniversityEmail().equalsIgnoreCase(tempUser.getUniversityEmail())){
            if(profileService.getUserByUniversityEmail(tempUser.getUniversityEmail())){
                throw new EmailAlreadyInUse("Email is already taken");
            }
        }
        tempUser.setRemarks("Update Request Pending Review");
        tempUser.setAccountStatus(AccountStatus.Pending);
        tempUser.setRequestAt(LocalDate.now());
        tempUserRepo.save(tempUser);
    }
    // Reject user info update request
    public void rejectTempUserRequest(TempUser tempUser, RemarkRequest remarkRequest){
        tempUser.setRemarks(remarkRequest.getMessage());
        tempUser.setAccountStatus(AccountStatus.Declined);
        tempUserRepo.save(tempUser);
    }
    public void approveRequest(TempUser tempUser){
        tempUser.setAccountStatus(AccountStatus.Approved);
        tempUser.setRemarks("Your Update Info Request has been Approved, changes have been applied");
        tempUserRepo.save(tempUser);
    }
    // get All Approval pending tempUser request
    public List<TempUser> getAllPending_Profiles(Integer pageNumber,Integer limit){
       return tempUserRepo.getAllPendingProfiles(PageRequest.of(pageNumber,limit));
    }
}
