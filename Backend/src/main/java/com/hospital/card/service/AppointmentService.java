package com.hospital.card.service;

import com.hospital.card.dto.AppointmentDTO;
import com.hospital.card.entity.Appointment;
import com.hospital.card.entity.Patient;
import com.hospital.card.entity.Staff;
import com.hospital.card.repository.AppointmentRepository;
import com.hospital.card.repository.PatientRepository;
import com.hospital.card.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

  private final AppointmentRepository appointmentRepository;
  private final PatientRepository patientRepository;
  private final StaffRepository staffRepository;

  public List<AppointmentDTO> getAllAppointments() {
    return appointmentRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
  }

  public AppointmentDTO getAppointment(Long id) {
    return appointmentRepository.findById(id).map(this::toDto)
        .orElseThrow(() -> new RuntimeException("Appointment not found"));
  }

  public AppointmentDTO createAppointment(AppointmentDTO dto) {
    Appointment a = new Appointment();

    if (dto.getPatientId() != null) {
      Patient patient = patientRepository.findById(dto.getPatientId())
          .orElseThrow(() -> new RuntimeException("Patient not found for appointment"));
      a.setPatient(patient);
    }

    if (dto.getDoctorId() != null) {
      Staff doctor = staffRepository.findById(dto.getDoctorId())
          .orElseThrow(() -> new RuntimeException("Doctor not found for appointment"));
      a.setDoctor(doctor);
    }

    a.setAppointmentDateTime(dto.getAppointmentDateTime());
    a.setStatus(dto.getStatus());
    a.setReason(dto.getReason());
    a.setNotes(dto.getNotes());

    Appointment saved = appointmentRepository.save(a);
    return toDto(saved);
  }

  public AppointmentDTO updateAppointment(Long id, AppointmentDTO dto) {
    Appointment a = appointmentRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Appointment not found"));

    if (dto.getPatientId() != null) {
      Patient patient = patientRepository.findById(dto.getPatientId())
          .orElseThrow(() -> new RuntimeException("Patient not found for appointment"));
      a.setPatient(patient);
    }

    if (dto.getDoctorId() != null) {
      Staff doctor = staffRepository.findById(dto.getDoctorId())
          .orElseThrow(() -> new RuntimeException("Doctor not found for appointment"));
      a.setDoctor(doctor);
    }

    if (dto.getAppointmentDateTime() != null)
      a.setAppointmentDateTime(dto.getAppointmentDateTime());
    if (dto.getStatus() != null)
      a.setStatus(dto.getStatus());
    if (dto.getReason() != null)
      a.setReason(dto.getReason());
    if (dto.getNotes() != null)
      a.setNotes(dto.getNotes());

    Appointment saved = appointmentRepository.save(a);
    return toDto(saved);
  }

  public void deleteAppointment(Long id) {
    appointmentRepository.deleteById(id);
  }

  private AppointmentDTO toDto(Appointment a) {
    AppointmentDTO dto = new AppointmentDTO();
    dto.setId(a.getId());
    if (a.getPatient() != null)
      dto.setPatientId(a.getPatient().getId());
    if (a.getDoctor() != null)
      dto.setDoctorId(a.getDoctor().getId());
    dto.setAppointmentDateTime(a.getAppointmentDateTime());
    dto.setStatus(a.getStatus());
    dto.setReason(a.getReason());
    dto.setNotes(a.getNotes());
    dto.setCreatedAt(a.getCreatedAt());
    return dto;
  }
}
