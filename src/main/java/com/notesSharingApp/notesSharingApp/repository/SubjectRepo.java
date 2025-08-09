package com.notesSharingApp.notesSharingApp.repository;

import com.notesSharingApp.notesSharingApp.model.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubjectRepo extends JpaRepository<Subject, UUID> {
    public Subject findByCode(String Code);
    @Query(value = "select * from subject limit 4",nativeQuery = true)
    public List<Subject> findByLimitedSubject();
    @Query(value = "select s from Subject s where lower(s.subjectName) like lower(concat('%', :query ,'%'))")
    Page<Subject> searchSubject(@Param("query") String query, Pageable pageable);
}
