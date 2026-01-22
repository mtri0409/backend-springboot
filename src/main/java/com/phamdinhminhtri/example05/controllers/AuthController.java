package com.phamdinhminhtri.example05.controllers;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.phamdinhminhtri.example05.entity.User;
import com.phamdinhminhtri.example05.exceptions.UserNotFoundException;
import com.phamdinhminhtri.example05.payloads.LoginCredentials;
import com.phamdinhminhtri.example05.payloads.UserDTO;
import com.phamdinhminhtri.example05.repo.UserRepo;
import com.phamdinhminhtri.example05.security.JWTUtil;
import com.phamdinhminhtri.example05.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "E-Commerce Application")
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    UserRepo userRepo;
    // Register a new user
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerHandler(@Valid @RequestBody UserDTO user) throws UserNotFoundException {
        
        // Encode password before saving
        String encodedPass = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPass);
        
        // Register user
        UserDTO userDTO = userService.registerUser(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(userDTO.getEmail());

        // Return token in response
        return new ResponseEntity<Map<String, Object>>(Collections.singletonMap("jwt-token", token), HttpStatus.CREATED);
    }

    // Login user and return JWT token
   @PostMapping("/login")
    public Map<String, Object> loginHandler(@RequestBody LoginCredentials credentials) {

        // 1️⃣ Authenticate
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                credentials.getEmail(),
                credentials.getPassword()
            )
        );

        // 2️⃣ LẤY USER ENTITY (CÓ CART CHẮC CHẮN)
        User user = userRepo.findByEmail(credentials.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 3️⃣ Generate JWT
        String token = jwtUtil.generateToken(credentials.getEmail());

        // 4️⃣ TRẢ ĐỦ DATA CHO FRONTEND
        return Map.of(
            "token", token,
            "email", user.getEmail(),
            "cartId", user.getCart().getCartId(),
            "username",user.getLastName()
        );
    }
}