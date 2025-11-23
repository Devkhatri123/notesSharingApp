package com.notesSharingApp.notesSharingApp.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.notesSharingApp.notesSharingApp.DTO.*;
import com.notesSharingApp.notesSharingApp.Enum.AccountStatus;
import com.notesSharingApp.notesSharingApp.Enum.Role;
import com.notesSharingApp.notesSharingApp.Enum.Status;
import com.notesSharingApp.notesSharingApp.Exception.*;
import com.notesSharingApp.notesSharingApp.Exception.Account.AccountIsBlocked;
import com.notesSharingApp.notesSharingApp.Exception.Account.AccountIsDisabled;
import com.notesSharingApp.notesSharingApp.Exception.Account.AccountNotFound;
import com.notesSharingApp.notesSharingApp.Exception.Account.EmailNotVerified;
import com.notesSharingApp.notesSharingApp.Exception.Note.FileNotSupported;
import com.notesSharingApp.notesSharingApp.Exception.Note.FileTooBig;
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
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private Cloudinary cloudinary;


    public void saveNote(Note note){
        notesRepo.save(note);
    }
    public void uploadNote(MultipartFile thumbnail, MultipartFile notes, UploadNoteDTO note) throws IOException,FileTooBig{
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
      if(notes.getContentType() != null && !notes.getContentType().equalsIgnoreCase("application/pdf")){  // applicaton/pdf
          throw new FileNotSupported("Only pdf notes are allowed");
      }
      if(thumbnail.getSize()/(1024 * 1024) > 2){
          throw new FileTooBig("Only 2mb thumbnail file size is allowed");
      }
      if(notes.getSize()/(1024 * 1024) > 20){
            throw new FileTooBig("Only 15mb pdf file size is allowed");
      }
      Subject subject = subjectService.getSubjectByCode(note.getSubjectCode());
      if(subject == null){
          throw new SubjectNotFound("Subject not found Against selected code");
      }
      Note n = modalMapper.map(note, Note.class);
      n.setSubject(subject);
      n.setCreatedBy(authenticatedUser.getUser());
      n.setImgThumbNail(uploadThumbnailToCloudinary(thumbnail));
      n.setThumbnailFilename(thumbnail.getOriginalFilename());
      n.setNotePdfData(uploadPdfToCloudinary(notes));
      n.setPdfNoteFilename(notes.getOriginalFilename());
      n.setStatus(Status.Pending);
      n.setRemarks("Pending review.");
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm:ss a", Locale.ENGLISH);
      n.setCreatedAt(LocalDateTime.now().format(formatter));
      if(n.getId().isBlank()) {
      n.setId(UUID.randomUUID().toString());
      }
      notesRepo.save(n);
   }

    public NotesDTO getNotesDTO(Note n) {
        NotesDTO notesDto = null;
        UserDTOWithoutNotes userDTOWithoutNotes = new UserDTOWithoutNotes();

        userDTOWithoutNotes.setId(n.getCreatedBy().getId());
        userDTOWithoutNotes.setUsername(n.getCreatedBy().getUsername());
        userDTOWithoutNotes.setSemester(n.getCreatedBy().getSemester());


         notesDto = modelMapper.map(n,NotesDTO.class);
//        notesDto.setId(n.getId());
//        notesDto.setTitle(n.getTitle());
//        notesDto.setDescription(n.getDescription());
//        notesDto.setCreatedAt(n.getCreatedAt());
         notesDto.setThumbnail(n.getImgThumbNail());
         notesDto.setNotes(n.getNotePdfData());
//        notesDto.setRemarks(n.getRemarks());
//        notesDto.setSubject(n.getSubject());
        notesDto.setStatus(n.getStatus().name());
        notesDto.setCreatedBy(userDTOWithoutNotes);
        return notesDto;
    }
   // Fetching subject notes
  public List<NotesDTO> getSubjectNotes(String subjectID, Integer pageNumber, Integer limit, String query) {
        List<Note> notes;
        if(query.isEmpty()) {
            notes = notesRepo.findBySubjectCode(subjectID, PageRequest.of(pageNumber, limit));
        }else{
            notes = notesRepo.findBySubjectCodeAndQuery(subjectID, PageRequest.of(pageNumber, limit),query);
        }
        return notes.stream().map(this::getNotesDTO).toList();
    }
    // Finding note in db by noteID
    public Note getNote(String noteID) throws NoteNotFound {
        if(notesRepo.existsById(noteID)) {
            Optional<Note> note = notesRepo.findById(noteID);
            return note.get();
        }
        throw new NoteNotFound("Note not found");
   }
   // Fetching approval pending notes
    public List<NotesDTO> getApprovalPendingNotes(Integer pageNumber, Integer limit) throws NotAllowed {
        userdetails authenticatedUser = util.getAuthenticatedUser();
        if((util.getAuthenticatedUser().getUser().getAccountStatus() == AccountStatus.Active && authenticatedUser.getUser().isEmailVerified()) || authenticatedUser.getUser().getRoles().contains(Role.MANAGER)){
        List<Note> approvalPendingNotes = notesRepo.findApprovalPendingNotes(PageRequest.of(pageNumber,limit));
            // Filtering notes, returning only admin's department notes for approval
            if(!authenticatedUser.getUser().getRoles().contains(Role.MANAGER)) {
                approvalPendingNotes = approvalPendingNotes.stream().filter(note -> {
                    // Matching admin's department notes
                    return note.getSubject().getDepartment().equals(authenticatedUser.getUser().getDepartment());
                }).toList();
            }
        return approvalPendingNotes.stream().map(this::getNotesDTO).toList();
        }else {
            throw new NotAllowed("Your are not allowed to view approval pending notes Requests. Your account is blocked or email isn't verified or you would have update you profile and your profile update request would be under review or you dont have permission.");
        }
        }

    public void sendRemark(RemarkRequest request) throws MessagingException,NoSuchElementException {
        // Finding reported note from db
     Optional<Note> note = notesRepo.findById(request.getId());
     if(note.isEmpty()){
         throw new NoSuchElementException("note doesn't exists");
     }
     Note foundNote = note.get();
     // setting remarks of note
     foundNote.setRemarks(request.getMessage());
     foundNote.setStatus(Status.Declined);
        // saving the same note with remarks and declined status
     notesRepo.save(foundNote);
     // sending the email to user about note has been declined
     sendRemarkEmail(foundNote.getCreatedBy().getUsername(), foundNote.getSubject().getSubjectName(),foundNote.getCreatedBy().getUniversityEmail(), request.getMessage());
    }
    private void sendRemarkEmail(String fullname,String subject,String to,String message) throws MessagingException {
        emailService.sendRemarkNotification(fullname,subject,to,message);
    }
    // Getting specfic user uploaded notes
   public List<NotesWithoutImagesDTO> myNotes(String userId,String status,Integer pageNumber,Integer limit){
        if (status.equals("All")) return getNotes(userId, pageNumber, limit);
        List<Note> notes = notesRepo.findNotesBycreatedBy(userId, Status.valueOf(status), PageRequest.of(pageNumber, limit));
        return notes.stream().map(this::convertToNotesWithoutImagesDTO).toList();

    }
    // Approve pending approval note by id
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

    public List<Note> getAllReportedNote(Integer pageNumber,Integer limit){
        return notesRepo.getAllReportedNote(PageRequest.of(pageNumber,limit));
    }
    // Delete note and send note removed email to note owner
    public void removeNote(String noteID,String removalReason) throws MessagingException,NoteNotFound {
      if(notesRepo.existsById(noteID)){
          Optional<Note> noteToBeRemoved = notesRepo.findById(noteID);
          if(noteToBeRemoved.isPresent()) {
              notesRepo.delete(noteToBeRemoved.get());
              emailService.sendNoteRemovalEmail(noteToBeRemoved.get(),removalReason);
          }else {
              throw new NoteNotFound("Note not found, note may already has been removed");
          }
      }
    }
    private String uploadThumbnailToCloudinary(MultipartFile thumbnail) throws IOException {
        Map uploadParams = ObjectUtils.asMap("resource_type", "image");
        Map uploadedFile =  cloudinary.uploader().upload(thumbnail.getBytes(),uploadParams);
      //String publicId = (String) uploadedFile.get("url");
        return (String) uploadedFile.get("secure_url");
    }
    private String uploadPdfToCloudinary(MultipartFile notePdf) throws IOException {
        Map uploadParams = ObjectUtils.asMap("resource_type", "raw");
        Map uploadedFile =  cloudinary.uploader().upload(notePdf.getBytes(),uploadParams);
        //String publicId = (String) uploadedFile.get("public_id");
        return (String) uploadedFile.get("secure_url");
    }
}
