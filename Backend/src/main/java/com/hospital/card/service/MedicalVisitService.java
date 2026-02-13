package com.hospital.card.service;

import com.hospital.card.dto.MedicalVisitDTO;
import com.hospital.card.entity.MedicalVisit;
import com.hospital.card.entity.Patient;
import com.hospital.card.entity.Staff;
import com.hospital.card.repository.MedicalVisitRepository;
import com.hospital.card.repository.PatientRepository;
import com.hospital.card.repository.StaffRepository;
import com.hospital.card.repository.HospitalCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalVisitService {

    private final MedicalVisitRepository medicalVisitRepository;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final HospitalCardRepository hospitalCardRepository;

    public List<MedicalVisitDTO> getAll() {
        return medicalVisitRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<MedicalVisitDTO> getByPatient(Long patientId) {
        return medicalVisitRepository.findByPatientId(patientId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<MedicalVisitDTO> getByDoctor(Long doctorId) {
        return medicalVisitRepository.findByDoctorId(doctorId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public MedicalVisitDTO create(MedicalVisitDTO dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Staff doctor = staffRepository.findById(dto.getDoctorId())
                .or(() -> staffRepository.findByUserId(dto.getDoctorId()))
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        MedicalVisit visit = new MedicalVisit();
        visit.setPatient(patient);
        visit.setDoctor(doctor);
        visit.setVisitDate(dto.getVisitDate() != null ? dto.getVisitDate() : LocalDate.now());
        visit.setDiagnosis(dto.getDiagnosis());
        visit.setPrescription(dto.getPrescription());
        visit.setLabTests(dto.getLabTests());
        visit.setFollowUpDate(dto.getFollowUpDate());
        visit.setStatus(dto.getStatus());
        visit.setContent(dto.getContent());
        visit.setAdditionalComments(dto.getAdditionalComments());

        MedicalVisit saved = medicalVisitRepository.save(visit);

        // Closing a case detaches the patient from the current doctor and removes
        // them from active access-card workflow.
        String normalizedStatus = dto.getStatus() == null ? "" : dto.getStatus().trim().toUpperCase();
        if ("CLOSED".equals(normalizedStatus) || "COMPLETED".equals(normalizedStatus)) {
            patient.setAssignedDoctor(null);
            patientRepository.save(patient);

            hospitalCardRepository.findByPatientId(patient.getId()).ifPresent(card -> {
                card.setStatus("INACTIVE");
                hospitalCardRepository.save(card);
            });
        }

        return toDto(saved);
    }

    private MedicalVisitDTO toDto(MedicalVisit visit) {
        MedicalVisitDTO dto = new MedicalVisitDTO();
        dto.setId(visit.getId());
        dto.setPatientId(visit.getPatient().getId());
        dto.setDoctorId(visit.getDoctor().getId());
        dto.setVisitDate(visit.getVisitDate());
        dto.setDiagnosis(visit.getDiagnosis());
        dto.setPrescription(visit.getPrescription());
        dto.setLabTests(visit.getLabTests());
        dto.setFollowUpDate(visit.getFollowUpDate());
        dto.setStatus(visit.getStatus());
        dto.setContent(visit.getContent());
        dto.setAdditionalComments(visit.getAdditionalComments());
        dto.setDoctorName(visit.getDoctor().getUser().getFirstName() + " " + visit.getDoctor().getUser().getLastName());
        dto.setCreatedAt(visit.getCreatedAt());
        return dto;
    }
}
