package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.DTO.RemarkRequest;
import com.notesSharingApp.notesSharingApp.DTO.userDTOWithoutNotes;
import com.notesSharingApp.notesSharingApp.Util.util;
import com.notesSharingApp.notesSharingApp.model.AccountStatus;
import com.notesSharingApp.notesSharingApp.model.TempUser;
import com.notesSharingApp.notesSharingApp.model.User;
import com.notesSharingApp.notesSharingApp.repository.ProfileRepo;
import com.notesSharingApp.notesSharingApp.repository.TempUserRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    // Sending user's info update request in tempUser table to let the admins
    // to-check whether the information in update request is appropriate, or not
    // if update information is valid, then tempUser row data will be copied in the main user table
    public void updateUser(String userId, TempUser user) throws RuntimeException{
        if(!util.isValidEmail(user.getUniversityEmail())){
            throw new RuntimeException("Email is not valid");
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
            throw new RuntimeException("No user Found");
        }
    }
    public TempUser ConvertToUserUpdateRequest(userDTOWithoutNotes user){
        return modelMapper.map(user, TempUser.class);
    }


    public List<TempUser> getApprovalPendingUsersInfo(Integer pageNumber, Integer limit) {
        return tempUserRepo.findAllByaccountStatus_(AccountStatus.Pending, PageRequest.of(pageNumber,limit));
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
                User RealUser = u2.get();
                RealUser.setAccountStatus(AccountStatus.Active);
                RealUser.setAccountRemarks("");
                profileRepo.save(RealUser);
            }
        }
    }

    public void approveChanges(String userId) {
        Optional<TempUser> u = tempUserRepo.findById(userId);
        Optional<User> u2 = profileRepo.findById(userId);
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
}
