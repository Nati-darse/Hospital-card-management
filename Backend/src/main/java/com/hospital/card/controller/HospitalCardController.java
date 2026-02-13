package com.hospital.card.controller;

import com.hospital.card.dto.HospitalCardDTO;
import com.hospital.card.entity.HospitalCard;
import com.hospital.card.service.HospitalCardService;
import com.hospital.card.service.UserService;
import com.hospital.card.entity.Patient;
import com.hospital.card.entity.Staff;
import com.hospital.card.entity.User;
import com.hospital.card.repository.HospitalCardRepository;
import com.hospital.card.repository.PatientRepository;
import com.hospital.card.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
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
  private final PatientRepository patientRepository;
  private final StaffRepository staffRepository;
  private final HospitalCardRepository hospitalCardRepository;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','USER')")
  public ResponseEntity<List<HospitalCardDTO>> getAll() {
    return ResponseEntity.ok(hospitalCardService.getAllCards());
  }

  @GetMapping("/assigned-patients")
  @PreAuthorize("hasAnyRole('ADMIN','USER')")
  public ResponseEntity<List<Map<String, Object>>> getAssignedPatients() {
    List<Map<String, Object>> assignedPatients = patientRepository.findAll().stream()
        .filter(patient -> patient.getUser() != null && Boolean.TRUE.equals(patient.getUser().getIsActive()))
        .filter(patient -> patient.getAssignedDoctor() != null)
        .map(patient -> {
          HospitalCard card = hospitalCardRepository.findByPatientId(patient.getId()).orElse(null);
          if (card == null || !"ACTIVE".equalsIgnoreCase(card.getStatus())) {
            return null;
          }

          User user = patient.getUser();
          Staff assignedDoctor = patient.getAssignedDoctor();
          boolean hasCaseHistory = userService.hasCaseHistoryForPatient(patient.getId());
          boolean hasAssignedDoctor = assignedDoctor != null;

          Map<String, Object> patientInfo = new HashMap<>();
          patientInfo.put("id", patient.getId());
          patientInfo.put("userId", user.getId());
          patientInfo.put("username", user.getUsername());
          patientInfo.put("firstName", user.getFirstName());
          patientInfo.put("lastName", user.getLastName());
          patientInfo.put("email", user.getEmail());
          patientInfo.put("phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
          patientInfo.put("medicalRecordNumber", patient.getMedicalRecordNumber());
          patientInfo.put("cardNumber", card.getCardNumber());
          patientInfo.put("cardId", card.getId());
          patientInfo.put("issueDate", card.getIssueDate());
          patientInfo.put("expiryDate", card.getExpiryDate());

          if (hasAssignedDoctor) {
            patientInfo.put("assignedDoctor", assignedDoctor.getUser().getFirstName() + " " + assignedDoctor.getUser().getLastName());
            patientInfo.put("assignedDoctorId", assignedDoctor.getId());
            patientInfo.put("department", assignedDoctor.getDepartment());
          } else {
            patientInfo.put("assignedDoctor", "Unassigned");
            patientInfo.put("assignedDoctorId", null);
            patientInfo.put("department", null);
          }

          patientInfo.put("hasCaseHistory", hasCaseHistory);
          patientInfo.put("status", hasCaseHistory ? "In Treatment" : "Pending Check-in");

          return patientInfo;
        })
        .filter(obj -> obj != null)
        .collect(Collectors.toList());
    
    return ResponseEntity.ok(assignedPatients);
  }

  @GetMapping("/available-doctors")
  @PreAuthorize("hasAnyRole('ADMIN','USER')")
  public ResponseEntity<List<Map<String, Object>>> getAvailableDoctors() {
    List<Map<String, Object>> doctors = staffRepository.findAll().stream()
        .filter(staff -> staff.getUser() != null && Boolean.TRUE.equals(staff.getUser().getIsActive()))
        .map(staff -> {
          Map<String, Object> doctorInfo = new HashMap<>();
          doctorInfo.put("id", staff.getUser().getId());
          doctorInfo.put("staffId", staff.getId());
          doctorInfo.put("userId", staff.getUser().getId());
          doctorInfo.put("firstName", staff.getUser().getFirstName());
          doctorInfo.put("lastName", staff.getUser().getLastName());
          doctorInfo.put("department", staff.getDepartment() != null ? staff.getDepartment() : "General");
          doctorInfo.put("fullName", staff.getUser().getFirstName() + " " + staff.getUser().getLastName());
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
      
      Optional<Patient> patientOpt = patientRepository.findById(patientId);
      if (patientOpt.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("error", "Patient not found"));
      }
      
      Optional<Staff> doctorOpt = staffRepository.findById(doctorId);
      if (doctorOpt.isEmpty()) {
        doctorOpt = staffRepository.findByUserId(doctorId);
      }
      if (doctorOpt.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("error", "Doctor not found"));
      }
      
      Patient patient = patientOpt.get();
      patient.setAssignedDoctor(doctorOpt.get());
      patientRepository.save(patient);

      return ResponseEntity.ok(Map.of("message", "Patient reassigned successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", "Failed to reassign patient: " + e.getMessage()));
    }
  }

  @PostMapping("/unassign-patient")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, String>> unassignPatient(@RequestBody Map<String, Object> request) {
    try {
      Long patientId = Long.valueOf(request.get("patientId").toString());
      Optional<Patient> patientOpt = patientRepository.findById(patientId);
      if (patientOpt.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("error", "Patient not found"));
      }

      Patient patient = patientOpt.get();
      patient.setAssignedDoctor(null);
      patientRepository.save(patient);

      return ResponseEntity.ok(Map.of("message", "Patient detached from doctor successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", "Failed to unassign patient: " + e.getMessage()));
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
