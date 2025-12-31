package com.hospital.card.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.card.dto.PatientDTO;
import com.hospital.card.service.PatientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final com.hospital.card.service.UserService userService;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','USER','PATIENT')")
    public ResponseEntity<PatientDTO> getMyProfile(java.security.Principal principal) {
        String username = principal.getName();
        com.hospital.card.entity.User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(patientService.getPatientByUserId(user.getId()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<PatientDTO>> getAll(@RequestParam(required = false) Long doctorId) {
        if (doctorId != null) {
            return ResponseEntity.ok(patientService.getPatientsByDoctor(doctorId));
        }
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<PatientDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatient(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER','PATIENT')")
    public ResponseEntity<PatientDTO> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(patientService.getPatientByUserId(userId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PatientDTO> create(@Valid @RequestBody PatientDTO dto) {
        PatientDTO created = patientService.createPatient(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<PatientDTO> update(@PathVariable Long id, @RequestBody PatientDTO dto) {
        return ResponseEntity.ok(patientService.updatePatient(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
