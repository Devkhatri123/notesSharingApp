package com.notesSharingApp.notesSharingApp.Service;


import com.notesSharingApp.notesSharingApp.DTO.RemarkRequest;
import com.notesSharingApp.notesSharingApp.Exception.Account.EmailAlreadyInUse;
import com.notesSharingApp.notesSharingApp.Exception.Account.EmailNotValid;
import com.notesSharingApp.notesSharingApp.Exception.Account.UsernameAlreadyTaken;
import com.notesSharingApp.notesSharingApp.Util.util;
import com.notesSharingApp.notesSharingApp.Enum.AccountStatus;
import com.notesSharingApp.notesSharingApp.model.TempUser;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import com.notesSharingApp.notesSharingApp.repository.TempUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class TempUserService {
    @Autowired
    private TempUserRepo tempUserRepo;
    @Autowired
    @Lazy
    private ProfileService profileService;

    public void save(TempUser tempUser) throws EmailNotValid,EmailAlreadyInUse,UsernameAlreadyTaken{
        if(!util.isValidEmail(tempUser.getUniversityEmail())){
            throw new EmailNotValid("Email is not valid");
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
}
