package com.hospital.card.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {
  private Long id;
  private Long patientId;
  private Long doctorId;
  private LocalDateTime appointmentDateTime;
  private String status;
  private String reason;
  private String notes;
  private LocalDateTime createdAt;
}
