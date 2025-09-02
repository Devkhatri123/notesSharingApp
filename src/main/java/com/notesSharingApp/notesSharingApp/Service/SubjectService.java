package com.notesSharingApp.notesSharingApp.Service;


import com.notesSharingApp.notesSharingApp.DTO.SubjectRequestDTO;
import com.notesSharingApp.notesSharingApp.DTO.SubjectResponseDTO;
import com.notesSharingApp.notesSharingApp.Exception.Account.NotLoggedIn;
import com.notesSharingApp.notesSharingApp.Exception.CharacterLimitExceeded;
import com.notesSharingApp.notesSharingApp.Exception.Subject.SubjectAlreadyExists;
import com.notesSharingApp.notesSharingApp.Exception.Subject.SubjectNotFound;
import com.notesSharingApp.notesSharingApp.Util.util;
import com.notesSharingApp.notesSharingApp.Enum.Status;
import com.notesSharingApp.notesSharingApp.model.Subject;
import com.notesSharingApp.notesSharingApp.model.User;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import com.notesSharingApp.notesSharingApp.repository.SubjectRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SubjectService {
    @Autowired
    private SubjectRepo subjectRepo;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private AuthenticationService authenticationService;

    public List<SubjectResponseDTO> getAllSubject(Integer pageNumber,Integer limit,String query){
        List<Subject> subjects = null;
        if(query == null) {
            subjects = subjectRepo.findAll(PageRequest.of(pageNumber, limit)).getContent();
        }else{
          subjects = subjectRepo.searchSubject(query,PageRequest.of(pageNumber,limit)).getContent();
        }
        if(util.getAuthenticatedUser() != null){
            User user = util.getAuthenticatedUser().getUser();
            subjects = subjects.stream().filter(subject -> {
            return subject.getSemester() == user.getSemester()
            &&
            subject.getDepartment().equalsIgnoreCase(user.getDepartment());
           }).toList();
        }
        return convertToSubjectResponseDtoList(subjects);
    }
    public Subject getSubjectByCode(String Code){
        return subjectRepo.findByCode(Code);
    }

    public List<SubjectResponseDTO> getAllSubjectOfUserDepartment(Integer pageNumber, Integer limit, String query, String department) {
       List<Subject> subjects = null;
       userdetails authenticatedUser = util.getAuthenticatedUser();
       if(!query.isEmpty()) subjects = subjectRepo.getSubjectsByUserDepartmentAndQuery(query,PageRequest.of(pageNumber,limit),department).getContent();
       else subjects = subjectRepo.findAllByDepartment(department,PageRequest.of(pageNumber,limit)).getContent();
       // Filtering subjects and returning only those subjects which are of admin's department
       subjects = subjects.stream().filter(subject -> {
           return subject.getDepartment().equalsIgnoreCase(authenticatedUser.getUser().getDepartment());
       }).toList();
       return convertToSubjectResponseDtoList(subjects);
 }
    public boolean isExistsById(String id){
        return subjectRepo.existsById(id);
    }
    public boolean isSubjectExistsByCodeOrTitle(String subjectName,String code){
        return subjectRepo.existsBySubjectNameOrCode(subjectName,code);
    }
    public void deleteSubject(String id){
        subjectRepo.deleteById(id);
    }
    private List<SubjectResponseDTO> convertToSubjectResponseDtoList(List<Subject> subjects){
        return subjects.stream().map(subject ->{
            SubjectResponseDTO subjectResponseDTO =  modelMapper.map(subject, SubjectResponseDTO.class);
            subjectResponseDTO.setCreatedById(subject.getCreatedBy().getId());
            subjectResponseDTO.setCreatedByName(subject.getCreatedBy().getUsername());
            if(subject.getUpdatedBy() != null){
                subjectResponseDTO.setEditedById(subject.getUpdatedBy().getId());
                subjectResponseDTO.setEditedByName(subject.getUpdatedBy().getUsername());
            }
            return subjectResponseDTO;
        }).toList();
    }
    // Admin Work
    // creating new subject and saving it in subject table
    public Subject addSubject(SubjectRequestDTO subjectRequestDTO) throws CharacterLimitExceeded {
        ValidateSubjectValues(subjectRequestDTO == null, subjectRequestDTO.getSubjectId(), subjectRequestDTO.getSubjectName(), subjectRequestDTO.getShortDescription(), subjectRequestDTO.getCode(), subjectRequestDTO.getSemester());
        // Checking does subject Already exists With provided Name or code.
        if(isSubjectExistsByCodeOrTitle(subjectRequestDTO.getSubjectName().replaceAll("\\s+",""), subjectRequestDTO.getCode().replaceAll("\\s+",""))){
            throw new SubjectAlreadyExists("Subject Already Exists. Try Other name or code");
        }
        userdetails userdetails = util.getAuthenticatedUser();
        Subject subject = null;

        subject = modelMapper.map(subjectRequestDTO,Subject.class);
        subject.setStatus(Status.Approved);
        subject.setCreatedAt(LocalDate.now());
        subject.setDepartment(userdetails.getUser().getDepartment());
        subject.setCreatedBy(userdetails.getUser());

        subjectRepo.save(subject);
        return subject;
    }

    private void ValidateSubjectValues(boolean b, String subjectId, String subjectName, String shortDescription, String code, int semester) {
      //  if(b || !subjectRepo.fin(subjectId)) throw new SubjectNotFound("Subject Not found");

        if(subjectName.trim().length() > 30 || subjectName.replaceAll("\\s+","").length() == 0) throw new CharacterLimitExceeded("Subject Name should be of 30 characters");

        if(shortDescription.trim().length() > 120 || shortDescription.replaceAll("\\s+","").length() == 0) throw new CharacterLimitExceeded("Subject Description should be of 120 characters");

        if(code.trim().length() > 7 || code.replaceAll("\\s+","").length() == 0) throw new CharacterLimitExceeded("Subject Code should be of 7 characters");

        if(semester <= 0 || semester > 8) throw new CharacterLimitExceeded("Invalid semester number");
    }

    public void updateSubject(SubjectResponseDTO subjectDto) throws CharacterLimitExceeded, SubjectNotFound {
        // Exception will be thrown if anything goes wrong in validation method
        if(!isExistsById(subjectDto.getSubjectId())) throw new SubjectNotFound("Subject Not found");
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

        subjectRepo.save(subject);
    }

    // Validate Subject values
    private void validateSubject(SubjectResponseDTO subject){
        // Checking the updating subject is present in Db or not
        ValidateSubjectValues(subject == null, subject.getSubjectId(), subject.getSubjectName(), subject.getShortDescription(), subject.getCode(), subject.getSemester());
    }
}
