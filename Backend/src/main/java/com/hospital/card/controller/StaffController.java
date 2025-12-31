package com.hospital.card.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final com.hospital.card.service.StaffService staffService;
    private final com.hospital.card.service.UserService userService;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<com.hospital.card.dto.StaffDTO> getMyProfile(java.security.Principal principal) {
        String username = principal.getName();
        com.hospital.card.entity.User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        com.hospital.card.dto.StaffDTO profile = staffService.getStaffByUserId(user.getId());
        return profile != null ? ResponseEntity.ok(profile) : ResponseEntity.notFound().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<com.hospital.card.dto.StaffDTO>> getAll() {
        return ResponseEntity.ok(staffService.getAllStaff());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<com.hospital.card.dto.StaffDTO> getById(@PathVariable Long id) {
        com.hospital.card.dto.StaffDTO dto = staffService.getStaffById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<com.hospital.card.dto.StaffDTO>> searchByDepartment(@RequestParam String department) {
        // We could add this to Service too, but for now let's keep it simple
        return ResponseEntity.ok(staffService.getAllStaff().stream()
                .filter(s -> s.getDepartment().equalsIgnoreCase(department))
                .collect(java.util.stream.Collectors.toList()));
    }
}
