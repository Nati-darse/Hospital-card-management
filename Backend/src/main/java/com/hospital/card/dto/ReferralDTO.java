package com.hospital.card.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferralDTO {
    private Long id;
    private Long patientId;
    private Long referringDoctorId;
    private Long referredDoctorId;
    private String department;
    private String reason;
    private String status;
    private LocalDate referralDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for UI
    private String patientName;
    private String referringDoctorName;
    private String referredDoctorName;
    private String patientMRN;
}
