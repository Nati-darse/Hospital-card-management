package com.hospital.card.controller;

import com.hospital.card.dto.UserDTO;
import com.hospital.card.entity.User;
import com.hospital.card.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
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
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toDto(userService.getUserById(id)));
    }

    @PostMapping
    public ResponseEntity<UserDTO> create(@Valid @RequestBody User user) {
        User created = userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable Long id, @RequestBody User user) {
        User updated = userService.updateUser(id, user);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
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
