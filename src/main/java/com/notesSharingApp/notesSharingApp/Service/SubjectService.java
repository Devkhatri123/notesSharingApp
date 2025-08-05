package com.notesSharingApp.notesSharingApp.Service;


import com.notesSharingApp.notesSharingApp.model.Subject;
import com.notesSharingApp.notesSharingApp.repository.SubjectRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService {
    @Autowired
    private SubjectRepo subjectRepo;

    public List<Subject> getAllSubject(Integer pageNumber,Integer limit){
         Page<Subject> subjects = subjectRepo.findAll(PageRequest.of(pageNumber,limit));
         return subjects.getContent();
    }
    public Subject getSubjectByCode(String Code){
        return subjectRepo.findByCode(Code);
    }

//    public List<Subject> getAllSubjectOfUserDepartmentAndSemester() {
//        userdetails authenticatedUser = (userdetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        return getAllSubject();
// }
}
