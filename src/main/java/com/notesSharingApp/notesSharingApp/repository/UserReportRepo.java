package com.notesSharingApp.notesSharingApp.repository;

import com.notesSharingApp.notesSharingApp.model.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReportRepo extends JpaRepository<UserReport,String> {
}
