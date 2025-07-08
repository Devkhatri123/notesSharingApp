package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.DTO.RemakRequest;
import com.notesSharingApp.notesSharingApp.DTO.notesDTO;
import com.notesSharingApp.notesSharingApp.DTO.userDTOWithoutNotes;
import com.notesSharingApp.notesSharingApp.model.Note;
import com.notesSharingApp.notesSharingApp.model.Subject;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.notesSharingApp.notesSharingApp.repository.notesRepo;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class notesService {

    @Autowired
    private subjectService subjectService;
    @Autowired
    private notesRepo notesRepo;
    @Autowired
    private emailService emailService;


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
      sendForApproval(n);

    }

    public static notesDTO getNotesDTO(Note n) {
        notesDTO notesDto = new notesDTO();
        userDTOWithoutNotes userDTOWithoutNotes = new userDTOWithoutNotes();

        userDTOWithoutNotes.setId(n.getCreatedBy().getId());
        userDTOWithoutNotes.setName(n.getCreatedBy().getFullname());
        userDTOWithoutNotes.setSemester(n.getCreatedBy().getSemester());
        userDTOWithoutNotes.setEmail(n.getCreatedBy().getEmail());

        notesDto.setId(n.getId());
        notesDto.setTitle(n.getTitle());
        notesDto.setDescription(n.getDescription());
        notesDto.setCreatedAt(n.getCreatedAt());
        notesDto.setThumbnail(n.getImgThumbNail());
        notesDto.setNotes(n.getNotePdfData());
        notesDto.setRemarks(n.getRemarks());
        notesDto.setSubject(n.getSubject());
        notesDto.setApproved(n.isApproved());
        notesDto.setCreatedBy(userDTOWithoutNotes);
        return notesDto;
    }


    public List<notesDTO> getSubjectNotes(String subjectID) {
        List<Note> notes = notesRepo.findBySubjectID(subjectID);
        List<notesDTO> notesDTOList = notes.stream().map(notesService::getNotesDTO).toList();
        return notesDTOList.stream().filter(notesDTO -> {
            return notesDTO.isApproved() && notesDTO.getRemarks() == null;
        }).toList();
    }

    public Note getNote(String noteID) throws RuntimeException {
       Optional<Note> note = notesRepo.findById(noteID);
       if(!note.isPresent()){
           throw new NoSuchElementException("This note doesn't exists");
       }

       if(!note.get().isApproved() && note.get().getRemarks() != null){
           throw new RuntimeException("This note is not available right now");
       }
       return note.get();
    }
    public List<notesDTO> getApprovalPendingNotes() {
        List<Note> allNotes = notesRepo.findByisApproved(false);
        return allNotes.stream().map(notesService::getNotesDTO).toList();
    }

    public void sendRemark(RemakRequest request) throws MessagingException {
     Optional<Note> note = notesRepo.findById(request.getId());
     if(!note.isPresent()){
         throw new NoSuchElementException("note doesn't exists");
     }
     Note foundNote = note.get();
     foundNote.setRemarks(request.getMessage());
     foundNote.setApproved(false);
     notesRepo.save(foundNote);
     sendRemarkEmail(foundNote.getCreatedBy().getFullname(), foundNote.getSubject().getSubjectName(),foundNote.getCreatedBy().getEmail(), request.getMessage());
    }
    private void sendRemarkEmail(String fullname,String subject,String to,String message) throws MessagingException {
        emailService.sendRemarkNotification(fullname,subject,to,message);
    }
    private void sendForApproval(Note note){
//        ApproveNote approveNote = new ApproveNote();
//        approveNote.setNote(note);
//
//        approveNoteRepo.save(approveNote);

    }
}
