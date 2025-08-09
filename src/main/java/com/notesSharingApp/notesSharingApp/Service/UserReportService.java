package com.notesSharingApp.notesSharingApp.Service;


import com.notesSharingApp.notesSharingApp.DTO.UserReportDTO;
import com.notesSharingApp.notesSharingApp.model.User;
import com.notesSharingApp.notesSharingApp.model.UserReport;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import com.notesSharingApp.notesSharingApp.repository.AuthenticationRepo;
import com.notesSharingApp.notesSharingApp.repository.UserReportRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserReportService {
    @Autowired
    private UserReportRepo reportRepo;
    @Autowired
    private AuthenticationRepo authenticationRepo;

    public void reportUser(UserReportDTO userReportDTO) {
        Optional<User> reportedUser = authenticationRepo.findByid(userReportDTO.getReportedUser());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        userdetails authenticatedUser = null;
        authenticatedUser = (userdetails) authentication.getPrincipal();
        User reportedBy = authenticatedUser.getUser();

        if(reportedUser.isPresent()) {

         UserReport userReport = new UserReport();

         userReport.setReportedUser(reportedUser.get());
         userReport.setReportedBy(reportedBy);
         userReport.setReportID(UUID.randomUUID().toString());
         userReport.setReason(userReportDTO.getReason());
         userReport.setAdditionalDetails(userReportDTO.getAdditionalDetails());


         if(!reportedUser.get().getReports().isEmpty())
         reportedUser.get().getReports().add(userReport);
         else {
             List<UserReport> reports = new ArrayList<>();
             reports.add(userReport);
             reportedUser.get().setReports(reports);
         }
            reportRepo.save(userReport);
            authenticationRepo.save(reportedBy);

        }else {
            throw new RuntimeException("No user found");
        }
   }
}
