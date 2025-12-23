package com.hospital.card.repository;

import com.hospital.card.entity.MedicalVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalVisitRepository extends JpaRepository<MedicalVisit, Long> {
    java.util.List<MedicalVisit> findByPatientId(Long patientId);

    java.util.List<MedicalVisit> findByDoctorId(Long doctorId);
}
