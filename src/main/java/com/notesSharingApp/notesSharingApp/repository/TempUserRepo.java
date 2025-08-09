package com.notesSharingApp.notesSharingApp.repository;

import com.notesSharingApp.notesSharingApp.model.AccountStatus;
import com.notesSharingApp.notesSharingApp.model.TempUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TempUserRepo extends JpaRepository<TempUser,String> {
    List<TempUser> findAllByaccountStatus_(AccountStatus accountStatus, PageRequest pageRequest);
    TempUser findOneByid(String id);
}
