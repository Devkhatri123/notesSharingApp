package com.notesSharingApp.notesSharingApp.Util;

import com.notesSharingApp.notesSharingApp.DTO.UserDTOWithoutNotes;
import com.notesSharingApp.notesSharingApp.model.User;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class util {
    public static UserDTOWithoutNotes convertUserModelToDTO(User user){
        final UserDTOWithoutNotes userDTOWithoutNotes = new UserDTOWithoutNotes();

        userDTOWithoutNotes.setId(user.getId());
        userDTOWithoutNotes.setUsername(user.getUsername());
        userDTOWithoutNotes.setUniversityEmail(user.getUniversityEmail());
        userDTOWithoutNotes.setSemester(user.getSemester());
        userDTOWithoutNotes.setDepartment(user.getDepartment());
        userDTOWithoutNotes.setEnabled(user.isEnabled());
        userDTOWithoutNotes.setAccountStatus(user.getAccountStatus().toString());
        userDTOWithoutNotes.setAccountRemarks(user.getAccountRemarks());
        userDTOWithoutNotes.setRoles(user.getRoles().stream().toList());
        userDTOWithoutNotes.setGender(user.getGender());
        userDTOWithoutNotes.setUniversityEmail(user.getUniversityEmail());
        userDTOWithoutNotes.setEmailVerified(user.isEmailVerified());

        return userDTOWithoutNotes;
    }
    public static boolean isValidEmail(String email) {
        boolean isValid = false;
        final List<String> UNIVERSITY_MAILS_REGEX = List.of("^csd\\d{2}(?:0[1-9]|1[0-2])\\d{2}+@dsu.edu.pk$", "^ce\\d{2}(?:0[1-9]|1[0-2])\\d{2}+@dsu.edu.pk$");
         for (String UNIVERSITY_MAIL_REGEX : UNIVERSITY_MAILS_REGEX){
            Pattern  pattern = Pattern.compile(UNIVERSITY_MAIL_REGEX);
            Matcher matcher = pattern.matcher(email);
            if(matcher.matches()) {
                isValid = matcher.matches();
                break;
            }
      }
        return isValid;
    }
    public static userdetails getAuthenticatedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()){
            if(authentication.getPrincipal() instanceof String){
                return null;
            }
            return (userdetails) authentication.getPrincipal();
        }
        return null;
    }
    public static int generateVerificationCode() {
        Random random = new Random();
        return random.nextInt(1000, 9999);
    }
}
