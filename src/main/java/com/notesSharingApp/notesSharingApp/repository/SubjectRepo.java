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
public interface SubjectRepo extends JpaRepository<Subject, String> {
    public Subject findByCode(String Code);
    @Query(value = "select * from subject limit 4",nativeQuery = true)
    public List<Subject> findByLimitedSubject();
    @Query(value = "select s from Subject s where lower(s.subjectName) like lower(concat('%', :query ,'%'))")
    Page<Subject> searchSubject(@Param("query") String query, Pageable pageable);

    @Query(value = "select s from Subject s where lower(s.subjectName) like lower(concat('%', :query ,'%')) or lower(s.status) like lower(concat('%', :query ,'%')) or" +
            " lower(s.code) like lower(concat('%', :query ,'%')) and s.department =:department")
    Page<Subject> getSubjectsByUserDepartmentAndQuery(@Param("query") String query, Pageable pageable,@Param("department") String department);

    Page<Subject> findAllByDepartment(String department, Pageable pageable);

    boolean existsBySubjectNameOrCode(String subjectName, String code);
}
