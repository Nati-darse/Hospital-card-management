package com.hospital.card.service;

import com.hospital.card.dto.AuthResponse;
import com.hospital.card.dto.LoginRequest;
import com.hospital.card.dto.RegisterRequest;
import com.hospital.card.entity.User;
import com.hospital.card.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    
    public AuthResponse register(RegisterRequest request) {
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setAddress(request.getAddress());
        
        // Save user
        User savedUser = userService.registerUser(user);
        
        // Generate token
        String token = jwtService.generateToken(savedUser.getUsername());
        
        // Update last login
        userService.updateLastLogin(savedUser.getUsername());
        
        return new AuthResponse(
            token,
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getRole(),
            savedUser.getFirstName(),
            savedUser.getLastName(),
            "Registration successful"
        );
    }
    
    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Get user details
        User user = userService.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate token
        String token = jwtService.generateToken(user.getUsername());
        
        // Update last login
        userService.updateLastLogin(user.getUsername());
        
        return new AuthResponse(
            token,
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.getFirstName(),
            user.getLastName(),
            "Login successful"
        );
    }
}