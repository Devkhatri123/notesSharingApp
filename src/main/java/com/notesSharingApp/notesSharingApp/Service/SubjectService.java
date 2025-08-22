package com.notesSharingApp.notesSharingApp.Service;


import com.notesSharingApp.notesSharingApp.DTO.SubjectResponseDTO;
import com.notesSharingApp.notesSharingApp.model.Subject;
import com.notesSharingApp.notesSharingApp.repository.SubjectRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectService {
    @Autowired
    private SubjectRepo subjectRepo;
    @Autowired
    private ModelMapper modelMapper;

    public List<SubjectResponseDTO> getAllSubject(Integer pageNumber,Integer limit,String query){
        Page<Subject> subjects = null;
        if(query == null) {
            subjects = subjectRepo.findAll(PageRequest.of(pageNumber, limit));
        }else{
          subjects = subjectRepo.searchSubject(query,PageRequest.of(pageNumber,limit));
        }
        return convertToSubjectResponseDtoList(subjects.getContent());
    }
    public Subject getSubjectByCode(String Code){
        return subjectRepo.findByCode(Code);
    }

    public List<SubjectResponseDTO> getAllSubjectOfUserDepartment(Integer pageNumber, Integer limit, String query, String department) {
       List<Subject> subjects = null;
       if(!query.isEmpty()) subjects = subjectRepo.getSubjectsByUserDepartmentAndQuery(query,PageRequest.of(pageNumber,limit),department).getContent();
       else subjects = subjectRepo.findAllByDepartment(department,PageRequest.of(pageNumber,limit)).getContent();

       return convertToSubjectResponseDtoList(subjects);
 }
    public void save(Subject subject){
        subjectRepo.save(subject);
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
            subjectResponseDTO.setCreatedByName(subject.getCreatedBy().getFullname());
            if(subject.getUpdatedBy() != null){
                subjectResponseDTO.setEditedById(subject.getUpdatedBy().getId());
                subjectResponseDTO.setEditedByName(subject.getUpdatedBy().getFullname());
            }
            return subjectResponseDTO;
        }).toList();
    }
}
