package com.hospital.card.controller;

import com.hospital.card.entity.Staff;
import com.hospital.card.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffRepository staffRepository;

    @GetMapping
    public ResponseEntity<List<Staff>> getAll() {
        return ResponseEntity.ok(staffRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Staff> getById(@PathVariable Long id) {
        return staffRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
