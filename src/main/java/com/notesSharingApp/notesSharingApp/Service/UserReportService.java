package com.notesSharingApp.notesSharingApp.Service;


import com.notesSharingApp.notesSharingApp.DTO.*;
import com.notesSharingApp.notesSharingApp.Exception.Account.AccountNotFound;
import com.notesSharingApp.notesSharingApp.Exception.Account.NotLoggedIn;
import com.notesSharingApp.notesSharingApp.Util.util;
import com.notesSharingApp.notesSharingApp.model.*;
import com.notesSharingApp.notesSharingApp.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserReportService {
    private final UserReportRepo reportRepo;
    private final ProfileService profileService;
    private final ModelMapper modelMapper;
    private final UserRepo userRepo;

    @Autowired
    public UserReportService(UserReportRepo reportRepo, ProfileService profileService, ModelMapper modelMapper, UserRepo userRepo){
        this.reportRepo = reportRepo;
        this.profileService = profileService;
        this.modelMapper = modelMapper;
        this.userRepo = userRepo;
    }

    public void reportUser(ReportRequestDTO reportRequestDTO) throws AccountNotFound,NotLoggedIn {
        User reportedUser = profileService.getuser(reportRequestDTO.getReportedUser());
        // Extracting the current authenticated user from securityContext to set reportedBy value in the report object
        userdetails authenticatedUser = util.getAuthenticatedUser();
        if(authenticatedUser != null) {
            User reportedBy = authenticatedUser.getUser();
            if (reportedUser != null) {
                UserReport userReport = modelMapper.map(reportRequestDTO, UserReport.class);
                userReport.setReportedUser(reportedUser);
                userReport.setReportedBy(reportedBy);
                reportRepo.save(userReport);
            } else {
                throw new AccountNotFound("Reported User not found");
            }
        }else {
            throw new NotLoggedIn("You are not loggedIn");
        }
   }
    public List<ReportedUserDTO> getReportedProfiles(Integer pageNumber, Integer limit) {
       List<User> reportedProfile = userRepo.getAllReportedProfile(PageRequest.of(pageNumber,limit));
        return reportedProfile.stream().map(user -> {
              ReportedUserDTO reportedUserDTO = modelMapper.map(user,ReportedUserDTO.class);
              reportedUserDTO.setReportCount((Long) userRepo.getReportCountOfProfile(reportedUserDTO.getId()).get(0)[0]);
              return reportedUserDTO;
       }).toList();
    }

    public List<ReportResponseDTO> getUserReports(String userId,Integer pageNumber, Integer limit) {
       List<UserReport> list = reportRepo.getAllReports(userId,PageRequest.of(pageNumber,limit));
       return list.stream().map(report -> {

       ReportResponseDTO  reportResponseDTO = modelMapper.map(report, ReportResponseDTO.class);
       reportResponseDTO.setReportedByUserId(report.getReportedBy().getId());
       reportResponseDTO.setReportedByUserName(report.getReportedBy().getUsername());
       reportResponseDTO.setReportedByUserEmail(report.getReportedBy().getUniversityEmail());
       return reportResponseDTO;
       }).toList();
    }
    public void deleteUserReports(String userId) {
        reportRepo.deleteById(userId);
    }
    public long reportedUserCount(){
        return reportRepo.countDistinctByreportedUser();
    }
}
