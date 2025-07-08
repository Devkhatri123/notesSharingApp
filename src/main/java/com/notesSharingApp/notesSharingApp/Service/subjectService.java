package com.notesSharingApp.notesSharingApp.Service;


import com.notesSharingApp.notesSharingApp.model.Subject;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import com.notesSharingApp.notesSharingApp.repository.subjectRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class subjectService {
    @Autowired
    private subjectRepo subjectRepo;

    public List<Subject> getAllSubject(){
        return subjectRepo.findAll();
    }
    public Subject getSubjectByCode(String Code){
        return subjectRepo.findByCode(Code);
    }

    public List<Subject> getAllSubjectOfUserDepartmentAndSemester() {
        userdetails authenticatedUser = (userdetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return getAllSubject();

    }
}
