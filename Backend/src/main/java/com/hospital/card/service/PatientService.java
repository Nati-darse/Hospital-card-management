package com.hospital.card.service;

import com.hospital.card.dto.PatientDTO;
import com.hospital.card.entity.Patient;
import com.hospital.card.entity.User;
import com.hospital.card.repository.PatientRepository;
import com.hospital.card.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public PatientDTO getPatient(Long id) {
        return patientRepository.findById(id).map(this::toDto)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    public PatientDTO createPatient(PatientDTO dto) {
        Patient p = new Patient();
        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found for patient"));
            p.setUser(user);
        }
        p.setMedicalRecordNumber(dto.getMedicalRecordNumber());
        p.setBloodGroup(dto.getBloodGroup());
        p.setEmergencyContactName(dto.getEmergencyContactName());
        p.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        p.setAllergies(dto.getAllergies());
        p.setChronicConditions(dto.getChronicConditions());
        p.setInsuranceProvider(dto.getInsuranceProvider());
        p.setInsuranceNumber(dto.getInsuranceNumber());

        Patient saved = patientRepository.save(p);
        return toDto(saved);
    }

    public PatientDTO updatePatient(Long id, PatientDTO dto) {
        Patient p = patientRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (dto.getMedicalRecordNumber() != null) p.setMedicalRecordNumber(dto.getMedicalRecordNumber());
        if (dto.getBloodGroup() != null) p.setBloodGroup(dto.getBloodGroup());
        if (dto.getEmergencyContactName() != null) p.setEmergencyContactName(dto.getEmergencyContactName());
        if (dto.getEmergencyContactPhone() != null) p.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        if (dto.getAllergies() != null) p.setAllergies(dto.getAllergies());
        if (dto.getChronicConditions() != null) p.setChronicConditions(dto.getChronicConditions());
        if (dto.getInsuranceProvider() != null) p.setInsuranceProvider(dto.getInsuranceProvider());
        if (dto.getInsuranceNumber() != null) p.setInsuranceNumber(dto.getInsuranceNumber());

        Patient saved = patientRepository.save(p);
        return toDto(saved);
    }

    public void deletePatient(Long id) {
        patientRepository.deleteById(id);
    }

    private PatientDTO toDto(Patient p) {
        PatientDTO dto = new PatientDTO();
        dto.setId(p.getId());
        if (p.getUser() != null) dto.setUserId(p.getUser().getId());
        dto.setMedicalRecordNumber(p.getMedicalRecordNumber());
        dto.setBloodGroup(p.getBloodGroup());
        dto.setEmergencyContactName(p.getEmergencyContactName());
        dto.setEmergencyContactPhone(p.getEmergencyContactPhone());
        dto.setAllergies(p.getAllergies());
        dto.setChronicConditions(p.getChronicConditions());
        dto.setInsuranceProvider(p.getInsuranceProvider());
        dto.setInsuranceNumber(p.getInsuranceNumber());
        return dto;
    }
}
