package com.hospital.card.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hospital.card.dto.PrescriptionDTO;
import com.hospital.card.entity.Patient;
import com.hospital.card.entity.Prescription;
import com.hospital.card.entity.Staff;
import com.hospital.card.repository.PrescriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientService patientService;
    private final StaffService staffService;

    public List<PrescriptionDTO> getPrescriptionsByPatient(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<PrescriptionDTO> getPrescriptionsByDoctor(Long doctorId) {
        return prescriptionRepository.findByDoctorId(doctorId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public PrescriptionDTO createPrescription(PrescriptionDTO dto) {
        Patient patient = patientService.getPatientEntity(dto.getPatientId());
        Staff doctor = staffService.getStaffEntity(dto.getDoctorId());

        Prescription p = new Prescription();
        p.setPatient(patient);
        p.setDoctor(doctor);
        p.setMedication(dto.getMedication());
        p.setDosage(dto.getDosage());
        p.setInstructions(dto.getInstructions());

        Prescription saved = prescriptionRepository.save(p);
        return toDto(saved);
    }

    private PrescriptionDTO toDto(Prescription p) {
        PrescriptionDTO dto = new PrescriptionDTO();
        dto.setId(p.getId());
        dto.setPatientId(p.getPatient().getId());
        dto.setDoctorId(p.getDoctor().getId());
        dto.setMedication(p.getMedication());
        dto.setDosage(p.getDosage());
        dto.setInstructions(p.getInstructions());
        dto.setPrescribedDate(p.getPrescribedDate());
        
        if (p.getPatient().getUser() != null) {
            dto.setPatientName(p.getPatient().getUser().getFirstName() + " " + p.getPatient().getUser().getLastName());
        }
        
        if (p.getDoctor().getUser() != null) {
            dto.setDoctorName(p.getDoctor().getUser().getFirstName() + " " + p.getDoctor().getUser().getLastName());
        }
        
        return dto;
    }
}
