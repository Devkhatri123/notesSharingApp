package com.notesSharingApp.notesSharingApp.repository;

import com.notesSharingApp.notesSharingApp.Enum.AccountStatus;
import com.notesSharingApp.notesSharingApp.model.TempUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TempUserRepo extends JpaRepository<TempUser,String> {
    @Query(value = "select u from TempUser u where u.accountStatus =\"Pending\"")
    List<TempUser> getAllPendingProfiles(PageRequest pageRequest);
    long countByaccountStatus(AccountStatus accountStatus);
    TempUser findOneByid(String id);
}
