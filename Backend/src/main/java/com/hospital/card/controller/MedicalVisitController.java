package com.hospital.card.controller;

import com.hospital.card.dto.MedicalVisitDTO;
import com.hospital.card.service.MedicalVisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class MedicalVisitController {

    private final MedicalVisitService medicalVisitService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<MedicalVisitDTO>> getAll() {
        return ResponseEntity.ok(medicalVisitService.getAll());
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER','PATIENT')")
    public ResponseEntity<List<MedicalVisitDTO>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicalVisitService.getByPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<MedicalVisitDTO>> getByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(medicalVisitService.getByDoctor(doctorId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<MedicalVisitDTO> create(@RequestBody MedicalVisitDTO visitDto) {
        return ResponseEntity.ok(medicalVisitService.create(visitDto));
    }
}
