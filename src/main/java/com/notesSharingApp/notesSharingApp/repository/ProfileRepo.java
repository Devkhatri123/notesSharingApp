package com.notesSharingApp.notesSharingApp.repository;

import com.notesSharingApp.notesSharingApp.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepo extends JpaRepository<User,String> {
    @Query("select u from User u left join fetch u.reports where u.id=:id")
    Optional<User> findByid(@Param("id")String id);
    @Query(value = "select distinct u.reportedUser from UserReport u")
    List<User> getAllReportedProfile(PageRequest pageRequest);
    @Query(value = "select count(u) from UserReport u where u.reportedUser.id=:userId")
    List<Object[]> getReportCountOfProfile(@Param("userId") String userId);
    @Query(value = "select u from User u where lower(u.fullname) like lower(concat('%', :Query ,'%')) or lower(u.universityEmail) like lower(concat('%', :Query ,'%')) ")
    List<User> searchByFullnameAndUniversityEmail(@Param("Query") String Query, Pageable pageable);
    @Query(value = "update User u set u.accountStatus = \"Active\",u.accountRemarks= \"\" where u.id=:userId ")
    @Modifying
    @Transactional
    void UnBlockUser(@Param("userId")String userId);

    boolean existsByUniversityEmail(String universityEmail);

}
