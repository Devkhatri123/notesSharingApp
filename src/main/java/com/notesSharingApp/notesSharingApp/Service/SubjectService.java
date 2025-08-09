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

    public List<Subject> getAllSubject(Integer pageNumber,Integer limit,String query){
        Page<Subject> subjects = null;
        if(query == null) {
            subjects = subjectRepo.findAll(PageRequest.of(pageNumber, limit));
        }else{
          subjects = subjectRepo.searchSubject(query,PageRequest.of(pageNumber,limit));
        }
         return subjects.getContent();
    }
    public Subject getSubjectByCode(String Code){
        return subjectRepo.findByCode(Code);
    }

}
