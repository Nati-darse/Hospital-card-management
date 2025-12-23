package com.hospital.card.controller;

import com.hospital.card.entity.MedicalVisit;
import com.hospital.card.repository.MedicalVisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class MedicalVisitController {

    private final MedicalVisitRepository medicalVisitRepository;

    @GetMapping
    public ResponseEntity<List<MedicalVisit>> getAll() {
        return ResponseEntity.ok(medicalVisitRepository.findAll());
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalVisit>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicalVisitRepository.findByPatientId(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<MedicalVisit>> getByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(medicalVisitRepository.findByDoctorId(doctorId));
    }

    @PostMapping
    public ResponseEntity<MedicalVisit> create(@RequestBody MedicalVisit visit) {
        if (visit.getVisitDate() == null) {
            visit.setVisitDate(LocalDate.now());
        }
        return ResponseEntity.ok(medicalVisitRepository.save(visit));
    }
}
