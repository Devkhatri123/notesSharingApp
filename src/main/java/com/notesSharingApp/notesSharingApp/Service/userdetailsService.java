package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.model.User;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.notesSharingApp.notesSharingApp.repository.AuthenticationRepo;



@Service
public class userdetailsService implements UserDetailsService {
    @Autowired
    private AuthenticationRepo authenticationRepo;

    @Override
    public userdetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = authenticationRepo.findByuniversityEmail(username);
        if(user == null){
            throw new UsernameNotFoundException("user not found");
        }

        return new userdetails(user);
    }
}
