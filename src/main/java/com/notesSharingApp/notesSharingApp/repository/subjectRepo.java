package com.notesSharingApp.notesSharingApp.repository;

import com.notesSharingApp.notesSharingApp.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface subjectRepo extends JpaRepository<Subject, UUID> {
    public Subject findByCode(String Code);
    @Query(value = "select * from subject limit 4",nativeQuery = true)
    public List<Subject> findByLimitedSubject();
}
