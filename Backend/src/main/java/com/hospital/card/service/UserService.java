package com.hospital.card.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hospital.card.entity.User;
import com.hospital.card.entity.UserRole;
import com.hospital.card.entity.Patient;
import com.hospital.card.repository.UserRepository;
import com.hospital.card.repository.PatientRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PatientRepository patientRepository;

    public User registerUser(User user) {
        // Check if username exists
        if (userRepository.existsByUsernameIgnoreCase(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Set default password if none provided
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            user.setPassword("Atlas@123");
            System.out.println("Setting default password 'Atlas@123' for new user: " + user.getUsername());
        }

        // Encode password with .encode func
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Default to true if null, otherwise respect the set value
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }

        User savedUser = userRepository.save(user);
        System.out.println("Successfully registered user: " + user.getUsername() + " with default password: Atlas@123");
        
        return savedUser;
    }

    public List<User> getPendingUsers() {
        return userRepository.findByIsActive(false);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public User updateLastLogin(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }

    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsActive(false);
        userRepository.save(user);
    }

    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsActive(true);
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUser(Long id, User update) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        if (update.getEmail() != null)
            user.setEmail(update.getEmail());
        if (update.getFirstName() != null)
            user.setFirstName(update.getFirstName());
        if (update.getLastName() != null)
            user.setLastName(update.getLastName());
        if (update.getPhoneNumber() != null)
            user.setPhoneNumber(update.getPhoneNumber());
        if (update.getDateOfBirth() != null)
            user.setDateOfBirth(update.getDateOfBirth());
        if (update.getGender() != null)
            user.setGender(update.getGender());
        if (update.getAddress() != null)
            user.setAddress(update.getAddress());
        if (update.getRole() != null)
            user.setRole(update.getRole());
        if (update.getIsActive() != null)
            user.setIsActive(update.getIsActive());
        if (update.getPassword() != null && !update.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(update.getPassword()));
        }

        return userRepository.save(user);
    }

    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password does not match");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public Optional<Patient> findPatientByUserId(Long userId) {
        return patientRepository.findByUserId(userId);
    }

    public boolean hasCaseHistoryForPatient(Long patientId) {
        // For now, return false since we don't have medical cases entity
        // This should be implemented when medical cases are added
        return false;
    }
}