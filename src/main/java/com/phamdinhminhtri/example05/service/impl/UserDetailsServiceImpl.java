package com.phamdinhminhtri.example05.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.phamdinhminhtri.example05.config.UserInfoConfig;
import com.phamdinhminhtri.example05.entity.User;
import com.phamdinhminhtri.example05.exceptions.ResourceNotFoundException;
import com.phamdinhminhtri.example05.repo.UserRepo;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{
    @Autowired
    private UserRepo userRepo;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        Optional<User> user = userRepo.findByEmail(username);
        return user.map(UserInfoConfig::new).orElseThrow(()-> new ResourceNotFoundException("user","email",username));
    }
}
