package com.hospital.card.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {
    private Long id;
    private Long userId;
    private String medicalRecordNumber;
    private String bloodGroup;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String allergies;
    private String chronicConditions;
    private String insuranceProvider;
    private String insuranceNumber;
    private Long assignedDoctorId;
    private UserDTO user;
    private StaffDTO assignedDoctor;
}
