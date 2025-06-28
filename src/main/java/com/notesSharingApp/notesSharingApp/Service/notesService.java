package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.DTO.notesDTO;
import com.notesSharingApp.notesSharingApp.DTO.userDTOWithoutNotes;
import com.notesSharingApp.notesSharingApp.model.Note;
import com.notesSharingApp.notesSharingApp.model.Subject;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.notesSharingApp.notesSharingApp.repository.notesRepo;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class notesService {

    @Autowired
    private subjectService subjectService;

    @Autowired
    private notesRepo notesRepo;


    public void uploadNote(MultipartFile thumbnail,MultipartFile notes,Note n) throws IOException {
      Subject subject = subjectService.getSubjectByCode(n.getSubjectCode());
      if(subject == null){
          throw new RuntimeException("Subject not found Against selected code");
      }
      n.setId(UUID.randomUUID().toString());
      n.setSubject(subject);
      userdetails authenticatedUser = (userdetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      n.setCreatedBy(authenticatedUser.getUser());
      n.setImgThumbNail(thumbnail.getBytes());
      n.setNotePdfData(notes.getBytes());
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm:ss a", Locale.ENGLISH);
      n.setCreatedAt(LocalDateTime.now().format(formatter));
      notesRepo.save(n);

    }

    public static notesDTO getNotesDTO(Note n) {
        notesDTO notesDto = new notesDTO();
        userDTOWithoutNotes userDTOWithoutNotes = new userDTOWithoutNotes();

        userDTOWithoutNotes.setId(n.getCreatedBy().getId());
        userDTOWithoutNotes.setName(n.getCreatedBy().getFullname());
        userDTOWithoutNotes.setSemester(n.getCreatedBy().getSemester());
        userDTOWithoutNotes.setUniversityEmail(n.getCreatedBy().getUniversityEmail());
        userDTOWithoutNotes.setEmail(n.getCreatedBy().getEmail());

        notesDto.setId(n.getId());
        notesDto.setTitle(n.getTitle());
        notesDto.setDescription(n.getDescription());
        notesDto.setCreatedAt(n.getCreatedAt());
        notesDto.setThumbnail(n.getImgThumbNail());
        notesDto.setNotes(n.getNotePdfData());
        notesDto.setSubject(n.getSubject());
        notesDto.setCreatedBy(userDTOWithoutNotes);
        return notesDto;
    }


}
