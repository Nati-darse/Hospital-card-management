package com.hospital.card.controller;

import com.hospital.card.dto.HospitalCardDTO;
import com.hospital.card.service.HospitalCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class HospitalCardController {

  private final HospitalCardService hospitalCardService;

  @GetMapping
  public ResponseEntity<List<HospitalCardDTO>> getAll() {
    return ResponseEntity.ok(hospitalCardService.getAllCards());
  }

  @GetMapping("/{id}")
  public ResponseEntity<HospitalCardDTO> getById(@PathVariable Long id) {
    return ResponseEntity.ok(hospitalCardService.getCard(id));
  }

  @PostMapping
  public ResponseEntity<HospitalCardDTO> create(@Valid @RequestBody HospitalCardDTO dto) {
    HospitalCardDTO created = hospitalCardService.createCard(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<HospitalCardDTO> update(@PathVariable Long id, @RequestBody HospitalCardDTO dto) {
    return ResponseEntity.ok(hospitalCardService.updateCard(id, dto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    hospitalCardService.deleteCard(id);
    return ResponseEntity.noContent().build();
  }
}
