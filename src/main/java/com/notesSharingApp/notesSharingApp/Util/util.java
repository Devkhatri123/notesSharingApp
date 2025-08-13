package com.notesSharingApp.notesSharingApp.Util;

import com.notesSharingApp.notesSharingApp.DTO.userDTOWithoutNotes;
import com.notesSharingApp.notesSharingApp.model.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class util {
    public static userDTOWithoutNotes convertUserModelToDTO(User user){
        final userDTOWithoutNotes userDTOWithoutNotes = new userDTOWithoutNotes();

        userDTOWithoutNotes.setId(user.getId());
        userDTOWithoutNotes.setFullname(user.getFullname());
        userDTOWithoutNotes.setUniversityEmail(user.getUniversityEmail());
        userDTOWithoutNotes.setSemester(user.getSemester());
        userDTOWithoutNotes.setDepartment(user.getDepartment());
        userDTOWithoutNotes.setEnabled(user.isEnabled());
        userDTOWithoutNotes.setAccountStatus(user.getAccountStatus().toString());
        userDTOWithoutNotes.setAccountRemarks(user.getAccountRemarks());
        userDTOWithoutNotes.setRole(user.getRole());
        userDTOWithoutNotes.setContact(user.getContact());
        userDTOWithoutNotes.setGender(user.getGender());
        userDTOWithoutNotes.setUniversityEmail(user.getUniversityEmail());
        userDTOWithoutNotes.setEmailVerified(user.isEmailVerified());

        return userDTOWithoutNotes;
    }
    public static boolean isValidEmail(String email) {
        String UNIVERSITY_MAIL_REGEX = "^csd\\d{2}(?:0[1-9]|1[0-2])\\d{2}+@dsu.edu.pk$";
        Pattern pattern = Pattern.compile(UNIVERSITY_MAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
