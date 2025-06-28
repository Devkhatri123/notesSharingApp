package com.notesSharingApp.notesSharingApp.Controller;

import com.notesSharingApp.notesSharingApp.DTO.notesDTO;
import com.notesSharingApp.notesSharingApp.model.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.notesSharingApp.notesSharingApp.Service.adminService;

import java.util.List;

@RestController
@RequestMapping("v1/admin")
public class adminController {

    @Autowired
    private adminService adminService;
    @GetMapping("/getApprovalPendingNotes")
    public List<notesDTO> getApprovalPendingNotes(){
       return adminService.getApprovalPendingNotes();
    }
}
