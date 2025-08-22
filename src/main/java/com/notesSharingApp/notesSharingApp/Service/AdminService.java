package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.DTO.SubjectRequestDTO;
import com.notesSharingApp.notesSharingApp.Exception.CharacterLimitExceeded;
import com.notesSharingApp.notesSharingApp.Exception.Subject.SubjectAlreadyExists;
import com.notesSharingApp.notesSharingApp.Exception.Subject.SubjectNotFound;
import com.notesSharingApp.notesSharingApp.Util.util;
import com.notesSharingApp.notesSharingApp.model.Status;
import com.notesSharingApp.notesSharingApp.model.Subject;
import com.notesSharingApp.notesSharingApp.model.userdetails;
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
        HashMap<String,Long> countCategories = new HashMap<>();
        countCategories.put("pendingNotes",notesService.getPendingNoteCount() > 0 ? notesService.getPendingNoteCount() : 0);
        countCategories.put("pendingUpdates",profileService.getPendingUpdatesProfiles() > 0 ? profileService.getPendingUpdatesProfiles() : 0);
        countCategories.put("reportedUser",reportService.reportedUserCount() > 0 ? reportService.reportedUserCount() : 0);
        return countCategories;
    }

    // creating new subject and saving it in subject table
    public Subject addSubject(SubjectRequestDTO subjectRequestDTO) {
        // Checking does subject Already exists With provided Name or code.
        if(subjectService.isSubjectExistsByCodeOrTitle(subjectRequestDTO.getSubjectName(), subjectRequestDTO.getCode())){
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

    public void updateSubject(Subject subject) {
       if(subject == null || !subjectService.isExistsById(subject.getSubjectId())) throw new SubjectNotFound("Subject Not found");
       if(subject.getSubjectName().length() > 30) throw new CharacterLimitExceeded("Subject Name should be of 30 character");
       if(subject.getShortDescription().length() > 120) throw new CharacterLimitExceeded("Subject Description should be of 120 character");
       if(subject.getCode().length() > 7) throw new CharacterLimitExceeded("Subject Code should be of 7 characters");
       if(subject.getSemester() <= 0 || subject.getSemester() > 8) throw new CharacterLimitExceeded("Invalid semester number");
       // set who updated the existing subject
        subject.setUpdatedBy(util.getAuthenticatedUser().getUser());
           // Set new values of existing subject
           subject.setSubjectName(subject.getSubjectName());
           subject.setSemester(subject.getSemester());
           subject.setCode(subject.getCode());
           subject.setShortDescription(subject.getShortDescription());
           subject.setUpdatedAt(LocalDate.now());

           subjectService.save(subject);
    }
}
