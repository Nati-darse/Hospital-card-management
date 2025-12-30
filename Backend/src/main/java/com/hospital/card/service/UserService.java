package com.hospital.card.service;

import com.hospital.card.entity.User;
import com.hospital.card.entity.UserRole;
import com.hospital.card.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        // Check if username exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if the email exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Encode password with .encode func
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Default to true if null, otherwise respect the set value
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }

        return userRepository.save(user);
    }

    public List<User> getPendingUsers() {
        return userRepository.findByIsActive(false);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public User updateLastLogin(String username) {
        User user = userRepository.findByUsername(username)
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
}