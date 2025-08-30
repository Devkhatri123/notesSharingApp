package com.notesSharingApp.notesSharingApp.repository;

import com.notesSharingApp.notesSharingApp.model.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,String> {
    public User findByuniversityEmail(@Param("universityEmail") String universityEmail);
    @Query(value = "select distinct u.reportedUser from UserReport u")
    List<User> getAllReportedProfile(PageRequest pageRequest);
    @Query(value = "select count(u) from UserReport u where u.reportedUser.id=:userId")
    List<Object[]> getReportCountOfProfile(@Param("userId") String userId);
    @Query(value = "select u from User u where lower(u.username) like lower(concat('%', :Query ,'%')) or lower(u.universityEmail) like lower(concat('%', :Query ,'%')) ")
    List<User> searchByFullnameAndUniversityEmail(@Param("Query") String Query, Pageable pageable);

    boolean existsByUniversityEmail(String universityEmail);

    Optional<User> findUserByUniversityEmail(String universityEmail);

    Optional<User> findOneByUniversityEmail(String universityEmail);

    boolean existsByUsername(String username);
}
