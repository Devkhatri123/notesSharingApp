package com.notesSharingApp.notesSharingApp.Service;


import com.notesSharingApp.notesSharingApp.Exception.EmailAlreadyInUse;
import com.notesSharingApp.notesSharingApp.Exception.EmailNotValid;
import com.notesSharingApp.notesSharingApp.Util.util;
import com.notesSharingApp.notesSharingApp.model.AccountStatus;
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

    public void save(TempUser tempUser){
        if(!util.isValidEmail(tempUser.getUniversityEmail())){
            throw new EmailNotValid("Email is not valid");
        }
        userdetails userdetails = util.getAuthenticatedUser();
        if(!userdetails.getUser().getUniversityEmail().equalsIgnoreCase(tempUser.getUniversityEmail())){
            if(profileService.getUserByUniversityEmail(tempUser.getUniversityEmail())){
                throw new EmailAlreadyInUse("Email is already taken");
            }
        }
        tempUser.setRemarks("Update Request Pending Review");
        tempUser.setAccountStatus(AccountStatus.Pending);
        tempUser.setRequestAt(LocalDate.now());
        tempUserRepo.save(tempUser);
    }
}
