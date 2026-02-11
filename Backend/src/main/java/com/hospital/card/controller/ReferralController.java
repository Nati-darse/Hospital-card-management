package com.hospital.card.controller;

import com.hospital.card.dto.ReferralDTO;
import com.hospital.card.service.ReferralService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/referrals")
@RequiredArgsConstructor
public class ReferralController {

    private final ReferralService referralService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<ReferralDTO>> getAll() {
        return ResponseEntity.ok(referralService.getAllReferrals());
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<ReferralDTO>> getReferralsForDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(referralService.getReferralsForDoctor(doctorId));
    }

    @GetMapping("/doctor/{doctorId}/pending")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<ReferralDTO>> getPendingReferralsForDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(referralService.getPendingReferralsForDoctor(doctorId));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER','PATIENT')")
    public ResponseEntity<List<ReferralDTO>> getReferralsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(referralService.getReferralsByPatient(patientId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ReferralDTO> createReferral(@RequestBody ReferralDTO referralDTO) {
        ReferralDTO created = referralService.createReferral(referralDTO);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{referralId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ReferralDTO> updateReferralStatus(
            @PathVariable Long referralId,
            @RequestParam String status) {
        ReferralDTO updated = referralService.updateReferralStatus(referralId, status);
        return ResponseEntity.ok(updated);
    }
}
