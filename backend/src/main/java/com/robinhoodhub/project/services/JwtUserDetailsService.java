package com.robinhoodhub.project.services;

import com.robinhoodhub.project.models.HubAccount;
import com.robinhoodhub.project.repositories.HubAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private HubAccountRepository hubAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        HubAccount hubAccount = hubAccountRepository.findByUsername(username);
        if(hubAccount==null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return new User(hubAccount.getUsername(), hubAccount.getPassword(), new ArrayList<>());
    }

}
