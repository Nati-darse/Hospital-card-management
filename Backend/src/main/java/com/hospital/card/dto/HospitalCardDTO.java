package com.hospital.card.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HospitalCardDTO {
  private Long id;
  private Long patientId;
  private String cardNumber;
  private String qrCodeData;
  private LocalDate issueDate;
  private LocalDate expiryDate;
  private String status;
}
