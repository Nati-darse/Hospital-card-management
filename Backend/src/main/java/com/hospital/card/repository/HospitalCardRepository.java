package com.hospital.card.repository;

import com.hospital.card.entity.HospitalCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HospitalCardRepository extends JpaRepository<HospitalCard, Long> {
    Optional<HospitalCard> findByCardNumber(String cardNumber);
    Optional<HospitalCard> findByPatientId(Long patientId);
    void deleteByPatientId(Long patientId);
}
