package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.DTO.RemarkRequest;
import com.notesSharingApp.notesSharingApp.DTO.NotesWithoutImagesDTO;
import com.notesSharingApp.notesSharingApp.DTO.notesDTO;
import com.notesSharingApp.notesSharingApp.DTO.userDTOWithoutNotes;
import com.notesSharingApp.notesSharingApp.Exception.NoteNotFound;
import com.notesSharingApp.notesSharingApp.model.Note;
import com.notesSharingApp.notesSharingApp.model.Status;
import com.notesSharingApp.notesSharingApp.model.Subject;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import jakarta.mail.MessagingException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.notesSharingApp.notesSharingApp.repository.notesRepo;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class notesService {

    @Autowired
    private subjectService subjectService;
    @Autowired
    private notesRepo notesRepo;
    @Autowired
    private emailService emailService;
    @Autowired
    private ModelMapper modelMapper;



    public void uploadNote(MultipartFile thumbnail,MultipartFile notes,Note n) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        userdetails object = null;
        if(authentication != null && authentication.isAuthenticated()) {
             object = (userdetails) authentication.getPrincipal();
            if(!object.getUser().isEmailVerified()){
                throw new RuntimeException("Your email is not verified. You are not allowed to upload the notes");
            }else if(!object.getUser().isEnabled()){
                throw new RuntimeException("Your account is disabled.You are not allowed to upload the notes");
            }

        }
      Subject subject = subjectService.getSubjectByCode(n.getSubjectCode());
      if(subject == null){
          throw new RuntimeException("Subject not found Against selected code");
      }
      n.setId(UUID.randomUUID().toString());
      n.setSubject(subject);
      userdetails authenticatedUser = (userdetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      n.setCreatedBy(authenticatedUser.getUser());
      n.setImgThumbNail(thumbnail.getBytes());
      n.setThumbnailFilename(thumbnail.getOriginalFilename());
      n.setNotePdfData(notes.getBytes());
      n.setPdfNoteFilename(notes.getOriginalFilename());
      n.setStatus(Status.Pending);
      n.setRemarks("Pending review.");
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


    public List<notesDTO> getSubjectNotes(String subjectID) {
        List<Note> notes = notesRepo.findBySubjectID(subjectID);
        return notes.stream().map(notesService::getNotesDTO).toList()
                .stream().filter(notesDTO -> {
                    return notesDTO.getStatus().equals("Approved");
                }).toList();
    }

    public Note getNote(String noteID) throws RuntimeException {
       Optional<Note> note = notesRepo.findById(noteID);
       if(!note.isPresent()){
           throw new NoSuchElementException("This note doesn't exists");
       }

       if(!note.get().getStatus().name().equals("Approved") && note.get().getRemarks() != null){
           throw new RuntimeException("This note is not available right now");
       }
       return note.get();
    }
    public List<notesDTO> getApprovalPendingNotes() {
        List<Note> allNotes = notesRepo.findAll();
        return allNotes.stream().map(notesService::getNotesDTO).toList()
                .stream().filter(noteDTO -> noteDTO.getStatus()
                        .equals("Pending")).toList();
     }

    public void sendRemark(RemarkRequest request) throws MessagingException {
     Optional<Note> note = notesRepo.findById(request.getId());
     if(!note.isPresent()){
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
    private void sendForApproval(Note note){
//        ApproveNote approveNote = new ApproveNote();
//        approveNote.setNote(note);
//
//        approveNoteRepo.save(approveNote);

    }

    public List<NotesWithoutImagesDTO> myNotes(String userId,String status,Integer pageNumber,Integer limit){
        if(status.equals("All")) return getNotes(userId,pageNumber, limit);
        List<Note> notes = notesRepo.findNotesBycreatedBy(userId,Status.valueOf(status),PageRequest.of(pageNumber,limit));
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
       return modelMapper.map(n,NotesWithoutImagesDTO.class);
    }


    public List<NotesWithoutImagesDTO> getNotes(String userId,Integer pageNumber,Integer limit){
        List<Note> allNotes =  notesRepo.findBycreatedBy(userId,PageRequest.of(pageNumber,limit));
        return allNotes.stream().map(this::convertToNotesWithoutImagesDTO).toList();
    }

    public Map<String,Long> getCountsOfNotes(String userId){
        List<Object[]> counts = notesRepo.getCountsOfNotes(userId);
        Map<String,Long> countsMap = counts.stream().collect(Collectors.toMap(a -> (String)a[0], a -> (Long) a[1]));
        return countsMap;
    }
    public void deleteNote(String noteID) {
         notesRepo.deleteById(noteID);
    }
}
