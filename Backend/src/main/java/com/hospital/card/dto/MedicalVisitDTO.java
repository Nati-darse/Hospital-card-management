package com.hospital.card.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalVisitDTO {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private LocalDate visitDate;
    private String diagnosis;
    private String prescription;
    private String labTests;
    private LocalDate followUpDate;
    private String status;
    private String content;
    private String additionalComments;
    private java.time.LocalDateTime createdAt;
}
