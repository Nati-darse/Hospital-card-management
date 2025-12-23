package com.hospital.card.service;

import com.hospital.card.dto.HospitalCardDTO;
import com.hospital.card.entity.HospitalCard;
import com.hospital.card.entity.Patient;
import com.hospital.card.repository.HospitalCardRepository;
import com.hospital.card.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HospitalCardService {

  private final HospitalCardRepository hospitalCardRepository;
  private final PatientRepository patientRepository;

  public List<HospitalCardDTO> getAllCards() {
    return hospitalCardRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
  }

  public HospitalCardDTO getCard(Long id) {
    return hospitalCardRepository.findById(id).map(this::toDto)
        .orElseThrow(() -> new RuntimeException("HospitalCard not found"));
  }

  public HospitalCardDTO createCard(HospitalCardDTO dto) {
    if (dto.getCardNumber() != null && hospitalCardRepository.findByCardNumber(dto.getCardNumber()).isPresent()) {
      throw new RuntimeException("Card number already exists: " + dto.getCardNumber());
    }

    HospitalCard card = new HospitalCard();

    if (dto.getPatientId() != null) {
      Patient patient = patientRepository.findById(dto.getPatientId())
          .orElseThrow(() -> new RuntimeException("Patient not found for card"));
      card.setPatient(patient);
    }

    if (dto.getCardNumber() == null) {
      // Improved card number generation: ATL-YYYY-RANDOM
      String year = String.valueOf(LocalDate.now().getYear());
      String random = String.format("%06d", (int) (Math.random() * 1000000));
      card.setCardNumber("ATL-" + year + "-" + random);
    } else {
      card.setCardNumber(dto.getCardNumber());
    }

    card.setQrCodeData(null); // No longer using QR codes
    card.setIssueDate(dto.getIssueDate() != null ? dto.getIssueDate() : LocalDate.now());
    card.setExpiryDate(dto.getExpiryDate() != null ? dto.getExpiryDate() : LocalDate.now().plusYears(5));
    card.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");

    HospitalCard saved = hospitalCardRepository.save(card);
    return toDto(saved);
  }

  public HospitalCardDTO updateCard(Long id, HospitalCardDTO dto) {
    HospitalCard card = hospitalCardRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("HospitalCard not found"));

    if (dto.getPatientId() != null) {
      Patient patient = patientRepository.findById(dto.getPatientId())
          .orElseThrow(() -> new RuntimeException("Patient not found for card"));
      card.setPatient(patient);
    }

    if (dto.getCardNumber() != null)
      card.setCardNumber(dto.getCardNumber());
    if (dto.getQrCodeData() != null)
      card.setQrCodeData(dto.getQrCodeData());
    if (dto.getIssueDate() != null)
      card.setIssueDate(dto.getIssueDate());
    if (dto.getExpiryDate() != null)
      card.setExpiryDate(dto.getExpiryDate());
    if (dto.getStatus() != null)
      card.setStatus(dto.getStatus());

    HospitalCard saved = hospitalCardRepository.save(card);
    return toDto(saved);
  }

  public void deleteCard(Long id) {
    hospitalCardRepository.deleteById(id);
  }

  private HospitalCardDTO toDto(HospitalCard card) {
    HospitalCardDTO dto = new HospitalCardDTO();
    dto.setId(card.getId());
    if (card.getPatient() != null)
      dto.setPatientId(card.getPatient().getId());
    dto.setCardNumber(card.getCardNumber());
    dto.setQrCodeData(card.getQrCodeData());
    dto.setIssueDate(card.getIssueDate());
    dto.setExpiryDate(card.getExpiryDate());
    dto.setStatus(card.getStatus());
    return dto;
  }
}
