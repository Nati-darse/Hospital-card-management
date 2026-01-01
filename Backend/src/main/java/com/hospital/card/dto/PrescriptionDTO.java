package com.hospital.card.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PrescriptionDTO {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private String patientName;
    private String doctorName;
    private String medication;
    private String dosage;
    private String instructions;
    private LocalDateTime prescribedDate;
}
