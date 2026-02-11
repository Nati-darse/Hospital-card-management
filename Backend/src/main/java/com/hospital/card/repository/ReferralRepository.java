package com.hospital.card.repository;

import com.hospital.card.entity.Referral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {
    
    List<Referral> findByReferredDoctorId(Long referredDoctorId);
    
    List<Referral> findByReferringDoctorId(Long referringDoctorId);
    
    List<Referral> findByPatientId(Long patientId);
    
    List<Referral> findByStatus(String status);
    
    @Query("SELECT r FROM Referral r WHERE r.referredDoctor.id = :doctorId AND r.status = 'Pending'")
    List<Referral> findPendingReferralsForDoctor(@Param("doctorId") Long doctorId);
    
    @Query("SELECT r FROM Referral r WHERE r.patient.id = :patientId ORDER BY r.createdAt DESC")
    List<Referral> findByPatientIdOrderByCreatedAtDesc(@Param("patientId") Long patientId);
}
