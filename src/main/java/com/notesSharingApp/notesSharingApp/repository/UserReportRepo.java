package com.notesSharingApp.notesSharingApp.repository;

import com.notesSharingApp.notesSharingApp.model.UserReport;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserReportRepo extends JpaRepository<UserReport,String> {
    @Query(value = "select u from UserReport u  where u.reportedUser.id=:userId")
    List<UserReport> getAllReports(@Param("userId") String userId, Pageable pageable);
    @Query(value = "delete from UserReport u where u.reportedUser.id=:userId")
    @Modifying
    @Transactional
    void deleteById(@Param("userId") String userId);
    @Query(value = "select count(distinct u.reportedUser) from UserReport u")
    long countDistinctByreportedUser();
}
