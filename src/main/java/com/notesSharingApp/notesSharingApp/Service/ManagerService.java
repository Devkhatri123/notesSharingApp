package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.Exception.AccountNotFound;
import com.notesSharingApp.notesSharingApp.model.Role;
import com.notesSharingApp.notesSharingApp.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ManagerService {
    @Autowired
    private ProfileService profileService;

    public void promoteUserToAdmin(String userID){
        User user = profileService.getuser(userID);
        if(user == null){
            throw new AccountNotFound("user not found");
        }
        Set<Role> roles = user.getRoles();
        roles.add(Role.ADMIN);
        user.setRoles(roles);
        profileService.saveUser(user);
    }
}
