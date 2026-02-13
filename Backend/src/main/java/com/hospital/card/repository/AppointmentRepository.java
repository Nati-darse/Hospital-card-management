package com.hospital.card.repository;

import com.hospital.card.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    java.util.List<Appointment> findByPatientId(Long patientId);
    java.util.List<Appointment> findByStatusIgnoreCase(String status);
    java.util.Optional<Appointment> findTopByPatientIdAndStatusIgnoreCaseOrderByCreatedAtDesc(Long patientId, String status);
    void deleteByPatientId(Long patientId);
}
