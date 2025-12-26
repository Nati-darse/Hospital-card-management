package com.hospital.card.repository;

import com.hospital.card.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    java.util.List<Bill> findByPatientId(Long patientId);
    void deleteByPatientId(Long patientId);
}
