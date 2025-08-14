package com.notesSharingApp.notesSharingApp.Service;


import com.notesSharingApp.notesSharingApp.DTO.ReportedUserDTO;
import com.notesSharingApp.notesSharingApp.DTO.UserReportRequestDTO;
import com.notesSharingApp.notesSharingApp.DTO.ReportResponseDTO;
import com.notesSharingApp.notesSharingApp.model.AccountStatus;
import com.notesSharingApp.notesSharingApp.model.User;
import com.notesSharingApp.notesSharingApp.model.UserReport;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import com.notesSharingApp.notesSharingApp.repository.AuthenticationRepo;
import com.notesSharingApp.notesSharingApp.repository.ProfileRepo;
import com.notesSharingApp.notesSharingApp.repository.UserReportRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Service
public class ReportService {
    @Autowired
    private UserReportRepo reportRepo;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ProfileRepo profileRepo;

    public void reportUser(UserReportRequestDTO userReportRequestDTO) {
        Optional<User> reportedUser = profileRepo.findByid(userReportRequestDTO.getReportedUser());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        userdetails authenticatedUser = null;
        authenticatedUser = (userdetails) authentication.getPrincipal();
        User reportedBy = authenticatedUser.getUser();
        User u = null;
        if(reportedUser.isPresent()) {
         u = reportedUser.get();
         UserReport userReport = new UserReport();

         userReport.setReportedUser(u);
         userReport.setReportedBy(reportedBy);
         userReport.setReason(userReportRequestDTO.getReason());
         userReport.setAdditionalDetails(userReportRequestDTO.getAdditionalDetails());

         reportRepo.save(userReport);

        }else {
            throw new RuntimeException("No user found");
        }
   }
    public List<ReportedUserDTO> getAllReports(Integer pageNumber, Integer limit) {
       List<User> reportedProfile = profileRepo.getAllReportedProfile(PageRequest.of(pageNumber,limit));
        return reportedProfile.stream().map(user -> {
              ReportedUserDTO reportedUserDTO = modelMapper.map(user,ReportedUserDTO.class);
              reportedUserDTO.setReportCount((Long) profileRepo.getReportCountOfProfile(reportedUserDTO.getId()).get(0)[0]);
              return reportedUserDTO;
       }).toList();
    }

    public List<ReportResponseDTO> getUserReports(String userId,Integer pageNumber, Integer limit) {
       List<UserReport> list = reportRepo.getAllReports(userId,PageRequest.of(pageNumber,limit));
       return list.stream().map(report -> {

           ReportResponseDTO reportResponseDTO = new ReportResponseDTO();
           reportResponseDTO.setReportID(report.getReportID());
           reportResponseDTO.setReason(report.getReason());
           reportResponseDTO.setAdditionalDetails(report.getAdditionalDetails());
           reportResponseDTO.setReportedByUserId(report.getReportedBy().getId());
           reportResponseDTO.setReportedByUserName(report.getReportedBy().getFullname());
           reportResponseDTO.setReportedByUserEmail(report.getReportedBy().getUniversityEmail());

           return reportResponseDTO;
       }).toList();
    }

    public void deleteUserReports(String userId) {
        reportRepo.deleteById(userId);
    }
}
