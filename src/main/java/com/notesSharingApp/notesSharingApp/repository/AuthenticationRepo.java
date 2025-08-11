package com.notesSharingApp.notesSharingApp.repository;

import com.notesSharingApp.notesSharingApp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthenticationRepo extends JpaRepository<User, String> {
   // @Query("select u from User u left join fetch u.reports where u.universityEmail=:universityEmail")
    public User findByuniversityEmail(@Param("universityEmail") String universityEmail);
    public User findBycontact(String phone);
    //@Query("select u from User u left join fetch u.reports where u.id=:id")
    Optional<User> findByid(@Param("id")String id);
}
