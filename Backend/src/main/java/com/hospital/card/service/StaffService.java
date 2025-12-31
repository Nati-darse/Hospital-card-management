package com.hospital.card.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hospital.card.dto.StaffDTO;
import com.hospital.card.dto.UserDTO;
import com.hospital.card.entity.Staff;
import com.hospital.card.repository.StaffRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;

    public List<StaffDTO> getAllStaff() {
        return staffRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public StaffDTO getStaffById(Long id) {
        return staffRepository.findById(id).map(this::toDto).orElse(null);
    }

    public StaffDTO getStaffByUserId(Long userId) {
        return staffRepository.findByUserId(userId).map(this::toDto).orElse(null);
    }

    public Staff getStaffEntity(Long id) {
        return staffRepository.findById(id).orElseThrow(() -> new RuntimeException("Staff not found"));
    }

    public StaffDTO toDto(Staff s) {
        if (s == null) return null;
        StaffDTO dto = new StaffDTO();
        dto.setId(s.getId());
        dto.setStaffId(s.getStaffId());
        dto.setDepartment(s.getDepartment());
        dto.setSpecialization(s.getSpecialization());
        dto.setQualification(s.getQualification());
        dto.setJoiningDate(s.getJoiningDate());
        if (s.getUser() != null) {
            dto.setUserId(s.getUser().getId());
            UserDTO userDto = new UserDTO();
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
