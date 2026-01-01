package com.hospital.card.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hospital.card.dto.PatientDTO;
import com.hospital.card.dto.UserDTO;
import com.hospital.card.entity.Patient;
import com.hospital.card.entity.User;
import com.hospital.card.repository.PatientRepository;
import com.hospital.card.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final StaffService staffService;
    private final com.hospital.card.repository.AppointmentRepository appointmentRepository;
    private final com.hospital.card.repository.MedicalVisitRepository medicalVisitRepository;
    private final com.hospital.card.repository.HospitalCardRepository hospitalCardRepository;
    private final com.hospital.card.repository.BillRepository billRepository;

    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public PatientDTO getPatient(Long id) {
        return patientRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    public List<PatientDTO> getPatientsByDoctor(Long doctorId) {
        return patientRepository.findByAssignedDoctorId(doctorId).stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    public PatientDTO getPatientByUserId(Long userId) {
        return patientRepository.findByUserId(userId).map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Patient profile not found for user: " + userId));
    }

    public PatientDTO createPatient(PatientDTO dto) {
        Patient p = new Patient();
        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found for patient"));
            p.setUser(user);
        }

        if (dto.getAssignedDoctorId() != null) {
            com.hospital.card.entity.Staff doctor = staffService.getStaffEntity(dto.getAssignedDoctorId());
            p.setAssignedDoctor(doctor);
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

        if (dto.getMedicalRecordNumber() != null)
            p.setMedicalRecordNumber(dto.getMedicalRecordNumber());
        if (dto.getBloodGroup() != null)
            p.setBloodGroup(dto.getBloodGroup());
        if (dto.getEmergencyContactName() != null)
            p.setEmergencyContactName(dto.getEmergencyContactName());
        if (dto.getEmergencyContactPhone() != null)
            p.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        if (dto.getAllergies() != null)
            p.setAllergies(dto.getAllergies());
        if (dto.getChronicConditions() != null)
            p.setChronicConditions(dto.getChronicConditions());
        if (dto.getInsuranceProvider() != null)
            p.setInsuranceProvider(dto.getInsuranceProvider());
        if (dto.getInsuranceNumber() != null)
            p.setInsuranceNumber(dto.getInsuranceNumber());
        if (dto.getAssignedDoctorId() != null) {
            com.hospital.card.entity.Staff doctor = staffService.getStaffEntity(dto.getAssignedDoctorId());
            p.setAssignedDoctor(doctor);
        }

        Patient saved = patientRepository.save(p);
        return toDto(saved);
    }

    public Patient getPatientEntity(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    public void deletePatient(Long id) {
        billRepository.deleteByPatientId(id);
        medicalVisitRepository.deleteAll(medicalVisitRepository.findByPatientId(id));
        appointmentRepository.deleteByPatientId(id);
        hospitalCardRepository.deleteByPatientId(id);
        patientRepository.deleteById(id);
    }



    private PatientDTO toDto(Patient p) {
        if (p == null) return null;
        PatientDTO dto = new PatientDTO();
        dto.setId(p.getId());
        if (p.getUser() != null) {
            dto.setUserId(p.getUser().getId());
            UserDTO userDto = new UserDTO();
            userDto.setId(p.getUser().getId());
            userDto.setUsername(p.getUser().getUsername());
            userDto.setEmail(p.getUser().getEmail());
            userDto.setFirstName(p.getUser().getFirstName());
            userDto.setLastName(p.getUser().getLastName());
            userDto.setPhoneNumber(p.getUser().getPhoneNumber());
            userDto.setAddress(p.getUser().getAddress());
            userDto.setGender(p.getUser().getGender());
            userDto.setDateOfBirth(p.getUser().getDateOfBirth());
            userDto.setRole(p.getUser().getRole());
            userDto.setIsActive(p.getUser().getIsActive());
            dto.setUser(userDto);
        }
        dto.setMedicalRecordNumber(p.getMedicalRecordNumber());
        dto.setBloodGroup(p.getBloodGroup());
        dto.setEmergencyContactName(p.getEmergencyContactName());
        dto.setEmergencyContactPhone(p.getEmergencyContactPhone());
        dto.setAllergies(p.getAllergies());
        dto.setChronicConditions(p.getChronicConditions());
        dto.setInsuranceProvider(p.getInsuranceProvider());
        dto.setInsuranceNumber(p.getInsuranceNumber());
        if (p.getAssignedDoctor() != null) {
            dto.setAssignedDoctorId(p.getAssignedDoctor().getId());
            dto.setAssignedDoctor(staffService.toDto(p.getAssignedDoctor()));
        }
        return dto;
    }
}
