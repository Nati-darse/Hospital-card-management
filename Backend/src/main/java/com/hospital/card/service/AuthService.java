package com.hospital.card.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hospital.card.dto.AuthResponse;
import com.hospital.card.dto.LoginRequest;
import com.hospital.card.dto.RegisterRequest;
import com.hospital.card.entity.User;
import com.hospital.card.entity.UserRole;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final com.hospital.card.repository.StaffRepository staffRepository;
    private final com.hospital.card.repository.PatientRepository patientRepository;

    public AuthResponse registerPatient(RegisterRequest request) {
        // Generate automatic username and password
        String generatedUsername = generateUsername(request.getFirstName(), request.getLastName());
        String generatedPassword = "Atlas@123";
        
        System.out.println("Generated username for patient: " + generatedUsername);
        System.out.println("Generated password for patient: " + generatedPassword);
        
        // Create new user
        User user = new User();
        user.setUsername(generatedUsername);
        user.setEmail(request.getEmail());
        user.setPassword(generatedPassword);
        user.setRole(UserRole.PATIENT); // Force Patient role
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setAddress(request.getAddress());
        user.setDepartment(request.getDepartment());
        user.setIsActive(false); // Inactive by default

        // Save user
        User savedUser = userService.registerUser(user);

        // Create Patient record
        com.hospital.card.entity.Patient patient = new com.hospital.card.entity.Patient();
        patient.setUser(savedUser);
        patient.setMedicalRecordNumber("MRN-" + System.currentTimeMillis() + "-" + savedUser.getId());
        patientRepository.save(patient);

        // No token returned for pending users
        return new AuthResponse(
                null,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                "Registration successful. Please wait for admin approval. Username: " + generatedUsername + ", Password: " + generatedPassword);
    }

    private String generateUsername(String firstName, String lastName) {
        // Base username: firstName + "_" + first 3 letters of lastName
        String baseUsername = firstName.toLowerCase() + "_" + lastName.toLowerCase().substring(0, Math.min(3, lastName.length()));
        
        // Check if base username exists
        String finalUsername = baseUsername;
        int counter = 1;
        
        while (userService.findByUsername(finalUsername).isPresent()) {
            finalUsername = baseUsername + counter;
            counter++;
        }
        
        return finalUsername;
    }

    // Admin uses this to create staff/other admins
    public User registerStaffOrAdmin(RegisterRequest request) {
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
        user.setDepartment(request.getDepartment());
        user.setIsActive(true); // Active by default

        User savedUser = userService.registerUser(user);

        // If role is USER (Doctor/Nurse), create Staff record
        if (savedUser.getRole() == com.hospital.card.entity.UserRole.USER) { 
            com.hospital.card.entity.Staff staff = new com.hospital.card.entity.Staff();
            staff.setUser(savedUser);
            staff.setDepartment(request.getDepartment());
            staff.setStaffId("STF-" + savedUser.getId());
            staffRepository.save(staff);
        } else if (savedUser.getRole() == com.hospital.card.entity.UserRole.PATIENT) {
            com.hospital.card.entity.Patient patient = new com.hospital.card.entity.Patient();
            patient.setUser(savedUser);
            patient.setMedicalRecordNumber("MRN-" + System.currentTimeMillis() + "-" + savedUser.getId());
            patientRepository.save(patient);
        }

        return savedUser;
    }

    public AuthResponse login(LoginRequest request) {
        System.out.println("Login attempt for username: " + request.getUsername());
        System.out.println("Password provided: " + (request.getPassword() != null && !request.getPassword().isEmpty() ? "[PRESENT]" : "[MISSING]"));
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details
            User user = userService.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            System.out.println("Found user: " + user.getUsername());
            System.out.println("User ID: " + user.getId());
            System.out.println("User role: " + user.getRole());
            System.out.println("User email: " + user.getEmail());
            System.out.println("User active: " + user.getIsActive());

            if (!user.getIsActive()) {
                throw new RuntimeException("Account is not active. Please contact administrator.");
            }

            // Update last login
            userService.updateLastLogin(user.getUsername());

            // Generate token
            String jwtToken = jwtService.generateToken(user);

            AuthResponse response = new AuthResponse(
                    jwtToken,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole(),
                    user.getFirstName(),
                    user.getLastName(),
                    "Login successful");

            System.out.println("AuthResponse created with ID: " + response.getUserId());
            return response;
        } catch (Exception e) {
            System.err.println("Login failed for username: " + request.getUsername());
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}