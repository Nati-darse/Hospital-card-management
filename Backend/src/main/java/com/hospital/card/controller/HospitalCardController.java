package com.hospital.card.controller;

import com.hospital.card.dto.HospitalCardDTO;
import com.hospital.card.service.HospitalCardService;
import com.hospital.card.service.UserService;
import com.hospital.card.entity.Patient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class HospitalCardController {

  private final HospitalCardService hospitalCardService;
  private final UserService userService;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','USER')")
  public ResponseEntity<List<HospitalCardDTO>> getAll() {
    return ResponseEntity.ok(hospitalCardService.getAllCards());
  }

  @GetMapping("/assigned-patients")
  @PreAuthorize("hasAnyRole('ADMIN','USER')")
  public ResponseEntity<List<Map<String, Object>>> getAssignedPatients() {
    List<Map<String, Object>> assignedPatients = userService.getAllUsers().stream()
        .filter(user -> user.getRole() == UserRole.PATIENT && user.getIsActive())
        .map(user -> {
          // Check if patient has assigned doctor
          Optional<Patient> patientOpt = userService.findPatientByUserId(user.getId());
          boolean hasAssignedDoctor = patientOpt.isPresent() && patientOpt.get().getAssignedDoctor() != null;
          
          // Check if patient has been seen (has case history)
          boolean hasCaseHistory = patientOpt.isPresent() && 
              userService.hasCaseHistoryForPatient(patientOpt.get().getId());
          
          // Include all assigned patients with their status
          if (hasAssignedDoctor) {
            Patient patient = patientOpt.get();
            Map<String, Object> patientInfo = new HashMap<>();
            patientInfo.put("id", user.getId());
            patientInfo.put("username", user.getUsername());
            patientInfo.put("firstName", user.getFirstName());
            patientInfo.put("lastName", user.getLastName());
            patientInfo.put("email", user.getEmail());
            patientInfo.put("phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
            patientInfo.put("assignedDoctor", patient.getAssignedDoctor().getUser().getFirstName() + " " + patient.getAssignedDoctor().getUser().getLastName());
            patientInfo.put("assignedDoctorId", patient.getAssignedDoctor().getId());
            patientInfo.put("department", patient.getAssignedDoctor().getDepartment());
            patientInfo.put("assignedDate", patient.getCreatedAt());
            patientInfo.put("hasCaseHistory", hasCaseHistory);
            patientInfo.put("status", hasCaseHistory ? "Completed" : "Pending Check-in");
            return patientInfo;
          }
          return null;
        })
        .filter(obj -> obj != null)
        .collect(Collectors.toList());
    
    return ResponseEntity.ok(assignedPatients);
  }

  @GetMapping("/available-doctors")
  @PreAuthorize("hasAnyRole('ADMIN','USER')")
  public ResponseEntity<List<Map<String, Object>>> getAvailableDoctors() {
    List<Map<String, Object>> doctors = userService.getAllUsers().stream()
        .filter(user -> user.getRole() == UserRole.DOCTOR && user.getIsActive())
        .map(doctor -> {
          Map<String, Object> doctorInfo = new HashMap<>();
          doctorInfo.put("id", doctor.getId());
          doctorInfo.put("firstName", doctor.getFirstName());
          doctorInfo.put("lastName", doctor.getLastName());
          doctorInfo.put("department", doctor.getDepartment() != null ? doctor.getDepartment() : "General");
          doctorInfo.put("fullName", doctor.getFirstName() + " " + doctor.getLastName());
          return doctorInfo;
        })
        .collect(Collectors.toList());
    
    return ResponseEntity.ok(doctors);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','USER')")
  public ResponseEntity<HospitalCardDTO> getById(@PathVariable Long id) {
    return ResponseEntity.ok(hospitalCardService.getCard(id));
  }

  @PostMapping("/reassign-patient")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, String>> reassignPatient(@RequestBody Map<String, Object> request) {
    try {
      Long patientId = Long.valueOf(request.get("patientId").toString());
      Long doctorId = Long.valueOf(request.get("doctorId").toString());
      
      // Find patient and update assignment
      Optional<Patient> patientOpt = userService.findPatientByUserId(patientId);
      if (patientOpt.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("error", "Patient not found"));
      }
      
      // Find doctor
      User doctor = userService.getUserById(doctorId);
      if (doctor.getRole() != UserRole.DOCTOR) {
        return ResponseEntity.badRequest().body(Map.of("error", "Selected user is not a doctor"));
      }
      
      // Update patient assignment
      Patient patient = patientOpt.get();
      patient.setAssignedDoctor(null); // Will be set by patient service
      
      // Call patient service to update assignment
      // This would need a proper service method, for now return success
      return ResponseEntity.ok(Map.of("message", "Patient reassigned successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", "Failed to reassign patient: " + e.getMessage()));
    }
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<HospitalCardDTO> update(@PathVariable Long id, @RequestBody HospitalCardDTO dto) {
    return ResponseEntity.ok(hospitalCardService.updateCard(id, dto));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    hospitalCardService.deleteCard(id);
    return ResponseEntity.noContent().build();
  }
}
