package com.hospital.card.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.card.entity.Staff;
import com.hospital.card.repository.StaffRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffRepository staffRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<com.hospital.card.dto.StaffDTO>> getAll() {
        List<com.hospital.card.dto.StaffDTO> list = staffRepository.findAll().stream()
                .map(this::toDto)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<com.hospital.card.dto.StaffDTO> getById(@PathVariable Long id) {
        return staffRepository.findById(id)
                .map(s -> ResponseEntity.ok(toDto(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<com.hospital.card.dto.StaffDTO>> searchByDepartment(@RequestParam String department) {
        List<com.hospital.card.dto.StaffDTO> list = staffRepository.findByDepartment(department).stream()
                .map(this::toDto)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(list);
    }

    private com.hospital.card.dto.StaffDTO toDto(Staff s) {
        com.hospital.card.dto.StaffDTO dto = new com.hospital.card.dto.StaffDTO();
        dto.setId(s.getId());
        dto.setStaffId(s.getStaffId());
        dto.setDepartment(s.getDepartment());
        dto.setSpecialization(s.getSpecialization());
        dto.setQualification(s.getQualification());
        dto.setJoiningDate(s.getJoiningDate());
        if (s.getUser() != null) {
            dto.setUserId(s.getUser().getId());
            com.hospital.card.dto.UserDTO userDto = new com.hospital.card.dto.UserDTO();
            userDto.setId(s.getUser().getId());
            userDto.setUsername(s.getUser().getUsername());
            userDto.setEmail(s.getUser().getEmail());
            userDto.setFirstName(s.getUser().getFirstName());
            userDto.setLastName(s.getUser().getLastName());
            userDto.setPhoneNumber(s.getUser().getPhoneNumber());
            userDto.setRole(s.getUser().getRole());
            userDto.setIsActive(s.getUser().getIsActive());
            dto.setUser(userDto);
        }
        return dto;
    }
}
