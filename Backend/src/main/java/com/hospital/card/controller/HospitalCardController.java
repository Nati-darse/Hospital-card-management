package com.hospital.card.controller;

import com.hospital.card.dto.HospitalCardDTO;
import com.hospital.card.service.HospitalCardService;
import com.hospital.card.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class HospitalCardController {

  private final HospitalCardService hospitalCardService;
  private final UserService userService;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','USER')")
  public ResponseEntity<List<HospitalCardDTO>> getAll() {
    return ResponseEntity.ok(hospitalCardService.getAllCards());
  }

  @GetMapping("/users-with-cards")
  @PreAuthorize("hasAnyRole('ADMIN','USER')")
  public ResponseEntity<List<Map<String, Object>>> getAllUsersWithCards() {
    List<Map<String, Object>> usersWithCards = userService.getAllUsers().stream()
        .map(user -> {
          Map<String, Object> userCardInfo = Map.of(
              "id", user.getId(),
              "username", user.getUsername(),
              "firstName", user.getFirstName(),
              "lastName", user.getLastName(),
              "email", user.getEmail(),
              "role", user.getRole().toString(),
              "isActive", user.getIsActive(),
              "phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : "",
              "department", user.getDepartment() != null ? user.getDepartment() : ""
          );
          return userCardInfo;
        })
        .collect(Collectors.toList());
    
    return ResponseEntity.ok(usersWithCards);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','USER')")
  public ResponseEntity<HospitalCardDTO> getById(@PathVariable Long id) {
    return ResponseEntity.ok(hospitalCardService.getCard(id));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<HospitalCardDTO> create(@Valid @RequestBody HospitalCardDTO dto) {
    HospitalCardDTO created = hospitalCardService.createCard(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<HospitalCardDTO> update(@PathVariable Long id, @RequestBody HospitalCardDTO dto) {
    return ResponseEntity.ok(hospitalCardService.updateCard(id, dto));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    hospitalCardService.deleteCard(id);
    return ResponseEntity.noContent().build();
  }
}
