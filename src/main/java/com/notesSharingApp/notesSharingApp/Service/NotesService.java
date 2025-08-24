package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.DTO.*;
import com.notesSharingApp.notesSharingApp.Exception.*;
import com.notesSharingApp.notesSharingApp.Exception.Subject.SubjectNotFound;
import com.notesSharingApp.notesSharingApp.Util.util;
import com.notesSharingApp.notesSharingApp.model.*;
import jakarta.mail.MessagingException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.notesSharingApp.notesSharingApp.repository.NotesRepo;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotesService {

    @Autowired
    private SubjectService subjectService;
    @Autowired
    private NotesRepo notesRepo;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ModelMapper modalMapper;

    public void uploadNote(MultipartFile thumbnail, MultipartFile notes, UploadNoteDTO note) throws IOException{
        userdetails authenticatedUser = util.getAuthenticatedUser();
        if(authenticatedUser != null) {
            if(!authenticatedUser.getUser().isEmailVerified()){
                throw new EmailNotVerified("Your email is not verified.You are not allowed to upload the notes");
            }
            if(authenticatedUser.getUser().getAccountStatus() == AccountStatus.Disabled){
                throw new AccountIsDisabled(authenticatedUser.getUser().getAccountRemarks());
            }
            else if(authenticatedUser.getUser().getAccountStatus() == AccountStatus.Blocked){
                throw new AccountIsBlocked(authenticatedUser.getUser().getAccountRemarks());
            }

        }else{
            throw new AccountNotFound("User is not authenticated");
        }

      if(note.getTitle().length() > 60){
          throw new CharacterLimitExceeded("Note title character limit is 100");
      }
      if(note.getDescription().length() > 300){
          throw new CharacterLimitExceeded("Notes description character limit is 300");
      }
      if(thumbnail.getContentType() !=null && !thumbnail.getContentType().equalsIgnoreCase("image/jpeg") && !thumbnail.getContentType().equalsIgnoreCase("image/jpg") && !thumbnail.getContentType().equalsIgnoreCase("image/png")){
          throw new FileNotSupported("Only jpeg/png/jpg thumbnails are allowed");
      }
      if(notes.getContentType() != null && !notes.getContentType().equalsIgnoreCase("application/pdf")){
          throw new FileNotSupported("Only pdf notes are allowed");
      }
      Subject subject = subjectService.getSubjectByCode(note.getSubjectCode());
      if(subject == null){
          throw new SubjectNotFound("Subject not found Against selected code");
      }
      Note n = modalMapper.map(note, Note.class);
      n.setSubject(subject);
      n.setCreatedBy(authenticatedUser.getUser());
      n.setImgThumbNail(thumbnail.getBytes());
      n.setNotePdfData(notes.getBytes());
      n.setStatus(Status.Pending);
      n.setRemarks("Pending review.");
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm:ss a", Locale.ENGLISH);
      n.setCreatedAt(LocalDateTime.now().format(formatter));
      if(n.getId().isBlank()) {
      n.setId(UUID.randomUUID().toString());
      }
      notesRepo.save(n);
   }

    public notesDTO getNotesDTO(Note n) {
        notesDTO notesDto = new notesDTO();
        userDTOWithoutNotes userDTOWithoutNotes = new userDTOWithoutNotes();

        userDTOWithoutNotes.setId(n.getCreatedBy().getId());
        userDTOWithoutNotes.setFullname(n.getCreatedBy().getFullname());
        userDTOWithoutNotes.setSemester(n.getCreatedBy().getSemester());

        notesDto.setId(n.getId());
        notesDto.setTitle(n.getTitle());
        notesDto.setDescription(n.getDescription());
        notesDto.setCreatedAt(n.getCreatedAt());
        notesDto.setThumbnail(n.getImgThumbNail());
        notesDto.setNotes(n.getNotePdfData());
        notesDto.setRemarks(n.getRemarks());
        notesDto.setSubject(n.getSubject());
        notesDto.setStatus(n.getStatus().name());
        notesDto.setCreatedBy(userDTOWithoutNotes);
        return notesDto;
    }


    public List<notesDTO> getSubjectNotes(String subjectID,Integer pageNumber,Integer limit,String query) {
        List<Note> notes;
        if(query.isEmpty()) {
            notes = notesRepo.findBySubjectCode(subjectID, PageRequest.of(pageNumber, limit));
        }else{
            notes = notesRepo.findBySubjectCodeAndQuery(subjectID, PageRequest.of(pageNumber, limit),query);
        }
        return notes.stream().map(this::getNotesDTO).toList();
    }

    public Note getNote(String noteID) throws RuntimeException {
        if(notesRepo.existsByIdAndStatus(noteID,Status.Approved)) {
            Optional<Note> note = notesRepo.findById(noteID);
            return note.get();
        }
        throw new NoteNotFound("Note not found");
   }
    public List<notesDTO> getApprovalPendingNotes(Integer pageNumber,Integer limit) {
        List<Note> approvalPendingNotes = notesRepo.findApprovalPendingNotes(PageRequest.of(pageNumber,limit));
        return approvalPendingNotes.stream().map(this::getNotesDTO).toList();
     }

    public void sendRemark(RemarkRequest request) throws MessagingException {
     Optional<Note> note = notesRepo.findById(request.getId());
     if(note.isEmpty()){
         throw new NoSuchElementException("note doesn't exists");
     }
     Note foundNote = note.get();
     foundNote.setRemarks(request.getMessage());
     foundNote.setStatus(Status.Declined);
     notesRepo.save(foundNote);
     sendRemarkEmail(foundNote.getCreatedBy().getFullname(), foundNote.getSubject().getSubjectName(),foundNote.getCreatedBy().getUniversityEmail(), request.getMessage());
    }
    private void sendRemarkEmail(String fullname,String subject,String to,String message) throws MessagingException {
        emailService.sendRemarkNotification(fullname,subject,to,message);
    }


    public List<NotesWithoutImagesDTO> myNotes(String userId,String status,Integer pageNumber,Integer limit){
        if (status.equals("All")) return getNotes(userId, pageNumber, limit);
        List<Note> notes = notesRepo.findNotesBycreatedBy(userId, Status.valueOf(status), PageRequest.of(pageNumber, limit));
        return notes.stream().map(this::convertToNotesWithoutImagesDTO).toList();

    }

    public void approveNote(String id) throws NoteNotFound {
     Optional<Note> note = notesRepo.findById(id);
     if(note.isPresent()){
        Note n = note.get();
        n.setRemarks("Approved");
        n.setStatus(Status.Approved);
        notesRepo.save(n);
        return;
     }
     throw new NoteNotFound("Note not found");
    }
    private NotesWithoutImagesDTO convertToNotesWithoutImagesDTO(Note n){
       return modalMapper.map(n,NotesWithoutImagesDTO.class);
    }


    public List<NotesWithoutImagesDTO> getNotes(String userId,Integer pageNumber,Integer limit){
        List<Note> allNotes =  notesRepo.findBycreatedBy(userId,PageRequest.of(pageNumber,limit));
        return allNotes.stream().map(this::convertToNotesWithoutImagesDTO).toList();
    }

    public Map<String,Long> getCountsOfNotes(String userId){
        List<Object[]> counts = notesRepo.getCountsOfNotes(userId);
        return counts.stream().collect(Collectors.toMap(a -> (String)a[0], a -> (Long) a[1]));
    }
    public void deleteNote(String noteID) {
         notesRepo.deleteByid(noteID);
    }
    public long getPendingNoteCount(){
        return notesRepo.countBystatus(Status.Pending);
    }
}
