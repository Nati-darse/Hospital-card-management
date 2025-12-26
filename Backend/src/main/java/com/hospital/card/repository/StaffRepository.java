package com.hospital.card.repository;

import com.hospital.card.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    java.util.List<Staff> findByDepartment(String department);
}
