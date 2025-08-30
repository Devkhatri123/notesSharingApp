package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.Enum.Role;
import com.notesSharingApp.notesSharingApp.Exception.Account.AccountNotFound;
import com.notesSharingApp.notesSharingApp.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ManagerService {
    private final ProfileService profileService;

    @Autowired
    public ManagerService(ProfileService profileService){
        this.profileService = profileService;
    }

    public void addAdminRole(String userID) throws AccountNotFound {
        User user = profileService.getuser(userID);
        Set<Role> roles = user.getRoles();
        roles.add(Role.ADMIN);
        user.setRoles(roles);
        profileService.saveUser(user);
    }
    public void removeAdminRole(String userId) throws AccountNotFound {
        User user = profileService.getuser(userId);
        Set<Role> roles = user.getRoles();
        roles.remove(Role.ADMIN);
        user.setRoles(roles);
        profileService.saveUser(user);
    }
}
