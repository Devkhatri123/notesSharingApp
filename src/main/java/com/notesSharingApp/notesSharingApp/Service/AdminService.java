package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.DTO.SubjectRequestDTO;
import com.notesSharingApp.notesSharingApp.DTO.SubjectResponseDTO;
import com.notesSharingApp.notesSharingApp.Exception.Account.NotLoggedIn;
import com.notesSharingApp.notesSharingApp.Exception.CharacterLimitExceeded;
import com.notesSharingApp.notesSharingApp.Exception.NotAllowed;
import com.notesSharingApp.notesSharingApp.Exception.Subject.SubjectAlreadyExists;
import com.notesSharingApp.notesSharingApp.Exception.Subject.SubjectNotFound;
import com.notesSharingApp.notesSharingApp.Util.util;
import com.notesSharingApp.notesSharingApp.model.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;

@Service
public class AdminService {
    @Autowired
    private NotesService notesService;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private ModelMapper modelMapper;

    public HashMap<String,Long> getCounts(){
        userdetails authenticatedUser = util.getAuthenticatedUser();
        if(authenticatedUser == null){
            throw new NotLoggedIn("You are not logged in");
        }
        HashMap<String,Long> countCategories = new HashMap<>();
        countCategories.put("pendingNotes",notesService.getPendingNoteCount() > 0 ? notesService.getPendingNoteCount() : 0);
        countCategories.put("pendingUpdates",profileService.getPendingUpdatesProfiles() > 0 ? profileService.getPendingUpdatesProfiles() : 0);
        countCategories.put("reportedUser",reportService.reportedUserCount() > 0 ? reportService.reportedUserCount() : 0);
        return countCategories;
    }

    // creating new subject and saving it in subject table
    public Subject addSubject(SubjectRequestDTO subjectRequestDTO) {
        // Checking does subject Already exists With provided Name or code.
        if(subjectService.isSubjectExistsByCodeOrTitle(subjectRequestDTO.getSubjectName().replaceAll("\\s+",""), subjectRequestDTO.getCode().replaceAll("\\s+",""))){
         throw new SubjectAlreadyExists("Subject Already Exists. Try Other name or code");
        }
        userdetails userdetails = util.getAuthenticatedUser();
        Subject subject = null;

        subject = modelMapper.map(subjectRequestDTO,Subject.class);
        subject.setStatus(Status.Approved);
        subject.setCreatedAt(LocalDate.now());
        subject.setDepartment(userdetails.getUser().getDepartment());
        subject.setCreatedBy(userdetails.getUser());

        subjectService.save(subject);
        return subject;
    }

    public void deleteSubject(String id) {
        subjectService.deleteSubject(id);
    }

    public void updateSubject(SubjectResponseDTO subjectDto) throws CharacterLimitExceeded,SubjectNotFound {
        // Exception will be thrown if anything goes wrong in validation method
        validateSubject(subjectDto);

        if(util.getAuthenticatedUser() == null){
            throw new NotLoggedIn("You are not logged in");
        }
        // Setting the new values in the subject model object from subject dto
        // by using mapper to reduce boilerplate code
        Subject subject = modelMapper.map(subjectDto,Subject.class);
       // User createdBy = profileService.getuser(subjectDto.getCreatedById());
        subject.setCreatedBy(subject.getCreatedBy());

        // set who updated the existing subject by extracting user From authenticated User
        if(subject.getCreatedBy().getId().equals(util.getAuthenticatedUser().getUser().getId())) subject.setUpdatedBy(subject.getCreatedBy());
        else subject.setUpdatedBy(util.getAuthenticatedUser().getUser());
        subject.setUpdatedAt(LocalDate.now());

        subjectService.save(subject);
    }
    // Validate Subject values
    private void validateSubject(SubjectResponseDTO subject){
        // Checking the updating subject is present in Db or not
        if(subject == null || !subjectService.isExistsById(subject.getSubjectId())) throw new SubjectNotFound("Subject Not found");

        if(subject.getSubjectName().trim().length() > 30 || subject.getSubjectName().replaceAll("\\s+","").length() == 0) throw new CharacterLimitExceeded("Subject Name should be of 30 characters");

        if(subject.getShortDescription().trim().length() > 120 || subject.getShortDescription().replaceAll("\\s+","").length() == 0) throw new CharacterLimitExceeded("Subject Description should be of 120 characters");

        if(subject.getCode().trim().length() > 7 || subject.getCode().replaceAll("\\s+","").length() == 0) throw new CharacterLimitExceeded("Subject Code should be of 7 characters");

        if(subject.getSemester() <= 0 || subject.getSemester() > 8) throw new CharacterLimitExceeded("Invalid semester number");
    }
}
