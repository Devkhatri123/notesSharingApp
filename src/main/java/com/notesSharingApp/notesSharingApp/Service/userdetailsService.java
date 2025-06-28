package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.model.user;
import com.notesSharingApp.notesSharingApp.model.userdetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.notesSharingApp.notesSharingApp.repository.authenticationRepo;



@Service
public class userdetailsService implements UserDetailsService {
    @Autowired
    private authenticationRepo authenticationRepo;

    @Override
    public userdetails loadUserByUsername(String username) throws UsernameNotFoundException {
        user user = authenticationRepo.findByemail(username);
        if(user == null){
            throw new UsernameNotFoundException("user not found");
        }

        return new userdetails(user);
    }
}
