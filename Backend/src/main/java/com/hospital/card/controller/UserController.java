package com.hospital.card.controller;

import com.hospital.card.dto.ChangePasswordRequest;
import com.hospital.card.dto.UserDTO;
import com.hospital.card.entity.User;
import com.hospital.card.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAll(
            @RequestParam(required = false) com.hospital.card.entity.UserRole role) {
        List<User> users;
        if (role != null) {
            users = userService.getUsersByRole(role);
        } else {
            users = userService.getAllUsers();
        }
        List<UserDTO> dtoList = users.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toDto(userService.getUserById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> create(@Valid @RequestBody User user) {
        User created = userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> update(@PathVariable Long id, @RequestBody User user) {
        User updated = userService.updateUser(id, user);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changePassword(@PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {
        User user = userService.getUserById(id);
        user.setPassword(request.getNewPassword()); // UserService handles encoding in updateUser? No, need to encode
                                                    // here or use Service method
        // Actually UserService.updateUser handles encoding if password is present.
        // Let's reuse updateUser logic but we need to map the DTO to User entity or
        // just call update.

        User updatePayload = new User();
        updatePayload.setPassword(request.getNewPassword());
        userService.updateUser(id, updatePayload);

        return ResponseEntity.ok().build();
    }

    private UserDTO toDto(User u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole());
        dto.setFirstName(u.getFirstName());
        dto.setLastName(u.getLastName());
        dto.setPhoneNumber(u.getPhoneNumber());
        dto.setDateOfBirth(u.getDateOfBirth());
        dto.setGender(u.getGender());
        dto.setAddress(u.getAddress());
        dto.setIsActive(u.getIsActive());
        return dto;
    }
}
