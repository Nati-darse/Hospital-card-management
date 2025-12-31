package com.hospital.card.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hospital.card.entity.Staff;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    java.util.List<Staff> findByDepartment(String department);
    java.util.Optional<Staff> findByUserId(Long userId);
}
