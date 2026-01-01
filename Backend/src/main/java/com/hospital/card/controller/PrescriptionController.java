package com.hospital.card.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.card.dto.PrescriptionDTO;
import com.hospital.card.service.PrescriptionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER','PATIENT')")
    public ResponseEntity<List<PrescriptionDTO>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionsByPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<PrescriptionDTO>> getByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionsByDoctor(doctorId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<PrescriptionDTO> create(@RequestBody PrescriptionDTO dto) {
        return ResponseEntity.ok(prescriptionService.createPrescription(dto));
    }
}
