package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.model.User;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import com.notesSharingApp.notesSharingApp.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    @Autowired
    private UserRepo userRepo;

    @Override
    public userdetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByuniversityEmail(username);
        if(user == null){
            throw new UsernameNotFoundException("user not found");
        }

        return new userdetails(user);
    }
}
