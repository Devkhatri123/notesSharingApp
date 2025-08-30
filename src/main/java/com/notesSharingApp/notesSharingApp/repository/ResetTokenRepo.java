package com.notesSharingApp.notesSharingApp.repository;

import com.notesSharingApp.notesSharingApp.model.ResetToken;
import com.notesSharingApp.notesSharingApp.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResetTokenRepo extends JpaRepository<ResetToken,String> {
    @Modifying
    @Transactional
    @Query(value = "delete from ResetToken token where token.user.id=:userId ")
    void deleteTokenByUserId(@Param("userId") String userId);

    List<ResetToken> findByUserUniversityEmail(String userUniversityEmail);

    ResetToken findOneByUser(User user);
}
