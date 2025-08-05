package com.notesSharingApp.notesSharingApp.repository;

import com.notesSharingApp.notesSharingApp.model.Status;
import com.notesSharingApp.notesSharingApp.model.TempUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Map;

@Repository
public interface UserUpdateRequest extends JpaRepository<TempUser,String> {
    List<TempUser> findAllByaccountStatus_(Status accountStatus,PageRequest pageRequest);
    TempUser findOneByid(String id);
}
