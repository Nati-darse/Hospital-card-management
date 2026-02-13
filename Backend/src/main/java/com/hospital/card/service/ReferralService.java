package com.hospital.card.service;

import com.hospital.card.dto.ReferralDTO;
import com.hospital.card.entity.Referral;
import com.hospital.card.entity.Patient;
import com.hospital.card.entity.Staff;
import com.hospital.card.repository.ReferralRepository;
import com.hospital.card.repository.PatientRepository;
import com.hospital.card.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferralService {
    
    private final ReferralRepository referralRepository;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final com.hospital.card.repository.HospitalCardRepository hospitalCardRepository;
    private final NotificationService notificationService;
    
    public List<ReferralDTO> getAllReferrals() {
        return referralRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<ReferralDTO> getReferralsForDoctor(Long doctorId) {
        return referralRepository.findByReferredDoctorId(doctorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<ReferralDTO> getPendingReferralsForDoctor(Long doctorId) {
        return referralRepository.findPendingReferralsForDoctor(doctorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<ReferralDTO> getReferralsByPatient(Long patientId) {
        return referralRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public ReferralDTO createReferral(ReferralDTO referralDTO) {
        Referral referral = convertToEntity(referralDTO);
        
        // Validate entities exist
        Patient patient = patientRepository.findById(referralDTO.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        Staff referringDoctor = staffRepository.findById(referralDTO.getReferringDoctorId())
                .or(() -> staffRepository.findByUserId(referralDTO.getReferringDoctorId()))
                .orElseThrow(() -> new RuntimeException("Referring doctor not found"));
        Staff referredDoctor = staffRepository.findById(referralDTO.getReferredDoctorId())
                .or(() -> staffRepository.findByUserId(referralDTO.getReferredDoctorId()))
                .orElseThrow(() -> new RuntimeException("Referred doctor not found"));
        
        referral.setPatient(patient);
        referral.setReferringDoctor(referringDoctor);
        referral.setReferredDoctor(referredDoctor);

        // Move patient ownership to referred doctor so they are removed from current doctor's queue
        patient.setAssignedDoctor(referredDoctor);
        patientRepository.save(patient);
        hospitalCardRepository.findByPatientId(patient.getId()).ifPresent(card -> {
            card.setStatus("ACTIVE");
            hospitalCardRepository.save(card);
        });
        
        // Set default values
        if (referral.getStatus() == null) {
            referral.setStatus("Pending");
        }
        if (referral.getReferralDate() == null) {
            referral.setReferralDate(java.time.LocalDate.now());
        }
        
        Referral savedReferral = referralRepository.save(referral);
        
        // Create notification for the referred doctor
        createReferralNotification(savedReferral);
        
        return convertToDTO(savedReferral);
    }
    
    public ReferralDTO updateReferralStatus(Long referralId, String status) {
        Referral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new RuntimeException("Referral not found"));
        
        referral.setStatus(status);
        Referral updatedReferral = referralRepository.save(referral);
        
        return convertToDTO(updatedReferral);
    }
    
    private void createReferralNotification(Referral referral) {
        String title = "New Patient Referral";
        String message = String.format("You have been referred a new patient: %s (%s)", 
                referral.getPatient().getUser().getFirstName() + " " + referral.getPatient().getUser().getLastName(),
                referral.getPatient().getMedicalRecordNumber());
        
        notificationService.createNotification(
                referral.getReferredDoctor().getUser(),
                title,
                message,
                "referral",
                referral.getId()
        );
    }
    
    private ReferralDTO convertToDTO(Referral referral) {
        ReferralDTO dto = new ReferralDTO();
        dto.setId(referral.getId());
        dto.setPatientId(referral.getPatient().getId());
        dto.setReferringDoctorId(referral.getReferringDoctor().getId());
        dto.setReferredDoctorId(referral.getReferredDoctor().getId());
        dto.setDepartment(referral.getDepartment());
        dto.setReason(referral.getReason());
        dto.setStatus(referral.getStatus());
        dto.setReferralDate(referral.getReferralDate());
        dto.setNotes(referral.getNotes());
        dto.setCreatedAt(referral.getCreatedAt());
        dto.setUpdatedAt(referral.getUpdatedAt());
        
        // Additional fields for UI
        dto.setPatientName(referral.getPatient().getUser().getFirstName() + " " + referral.getPatient().getUser().getLastName());
        dto.setReferringDoctorName("Dr. " + referral.getReferringDoctor().getUser().getFirstName() + " " + referral.getReferringDoctor().getUser().getLastName());
        dto.setReferredDoctorName("Dr. " + referral.getReferredDoctor().getUser().getFirstName() + " " + referral.getReferredDoctor().getUser().getLastName());
        dto.setPatientMRN(referral.getPatient().getMedicalRecordNumber());
        
        return dto;
    }
    
    private Referral convertToEntity(ReferralDTO dto) {
        Referral referral = new Referral();
        referral.setDepartment(dto.getDepartment());
        referral.setReason(dto.getReason());
        referral.setStatus(dto.getStatus());
        referral.setReferralDate(dto.getReferralDate());
        referral.setNotes(dto.getNotes());
        return referral;
    }
}
