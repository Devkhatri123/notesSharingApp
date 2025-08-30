package com.notesSharingApp.notesSharingApp.Service;
import com.notesSharingApp.notesSharingApp.DTO.ReportNoteResponseDTO;
import com.notesSharingApp.notesSharingApp.DTO.ReportRequestDTO;
import com.notesSharingApp.notesSharingApp.DTO.ReportedNoteDTO;
import com.notesSharingApp.notesSharingApp.model.Note;
import com.notesSharingApp.notesSharingApp.model.NoteReport;
import com.notesSharingApp.notesSharingApp.model.User;
import com.notesSharingApp.notesSharingApp.repository.NoteReportsRepo;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NoteReportService {
    private final NoteReportsRepo noteReportsRepo;
    private final NotesService notesService;
    private final ModelMapper modelMapper;
    private final ProfileService profileService;

    @Autowired
    public NoteReportService(NoteReportsRepo noteReportsRepo, NotesService notesService, ProfileService profileService, ModelMapper modelMapper){
        this.noteReportsRepo = noteReportsRepo;
        this.notesService = notesService;
        this.profileService = profileService;
        this.modelMapper = modelMapper;
    }
    public void reportNote(ReportRequestDTO reportRequestDTO){
        Note reportedNote = notesService.getNote(reportRequestDTO.getReportedNote());
        User reportedByUser = profileService.getuser(reportRequestDTO.getReportedBy());

        // checking has user already reported the same note before, if yes, then in if condition we are just
        // updating report reason and additional Details, if no then in else statement we're setting all new details
        // like who reported the note, and which note has been reported.
        NoteReport report = noteReportsRepo.findByReportedByAndReportedNote(reportedByUser,reportedNote);
        if(report != null){
            // updating note report reason and additional details.
            report.setReason(reportRequestDTO.getReason());
            report.setAdditionalDetails(reportRequestDTO.getAdditionalDetails());
        }else {
            // Setting new details here
            report = modelMapper.map(reportRequestDTO, NoteReport.class);
            report.setReportedNote(reportedNote);
            report.setReportedBy(reportedByUser);
        }
        noteReportsRepo.save(report);
    }
    // Get ReportedNotes
    public List<ReportedNoteDTO> getAllReportedNote(Integer pageNumber, Integer limit){
        // Fetching reported Notes and converting each note into ReportedNoteDTO object to show only necessary information
        return notesService.getAllReportedNote(pageNumber,limit).stream().map(
        note -> {
        ReportedNoteDTO reportedNoteDTO = new ReportedNoteDTO();
        reportedNoteDTO.setNoteID(note.getId());
        reportedNoteDTO.setSubjectName(note.getSubject().getSubjectName());
        reportedNoteDTO.setNoteName(note.getTitle());
        reportedNoteDTO.setReportCount(noteReportsRepo.countNoteReportByReportedNoteId(note.getId()));
        return reportedNoteDTO;
        }).toList();
    }
    public List<ReportNoteResponseDTO> getNoteReports(Integer pageNumber, Integer limit, String noteID){
        // Fetching specific note reports and converting each report into
        // ReportNoteResponseDTO Object
        return noteReportsRepo.getAllByReportedNoteId(noteID,PageRequest.of(pageNumber,limit))
        .stream().map(noteReport -> {
        ReportNoteResponseDTO reportNoteResponseDTO = modelMapper.map(noteReport, ReportNoteResponseDTO.class);
        reportNoteResponseDTO.setReportedByName(noteReport.getReportedBy().getUsername());
        return reportNoteResponseDTO;
        }).toList();
    }
    @Transactional
    public void discardReports(String noteID) {
        noteReportsRepo.deleteByReportedNoteId(noteID);
    }
}
