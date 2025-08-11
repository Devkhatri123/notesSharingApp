package com.notesSharingApp.notesSharingApp.repository;

import com.notesSharingApp.notesSharingApp.model.UserReport;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserReportRepo extends JpaRepository<UserReport,String> {
    @Query(value = "select u from UserReport u  where u.reportedUser.id=:userId")
    List<UserReport> getAllReports(@Param("userId") String userId);
}
