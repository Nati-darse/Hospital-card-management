package com.hospital.card.controller;

import com.hospital.card.dto.AppointmentDTO;
import com.hospital.card.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

  private final AppointmentService appointmentService;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','USER')")
  public ResponseEntity<List<AppointmentDTO>> getAll() {
    return ResponseEntity.ok(appointmentService.getAllAppointments());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','USER')")
  public ResponseEntity<AppointmentDTO> getById(@PathVariable Long id) {
    return ResponseEntity.ok(appointmentService.getAppointment(id));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','USER')")
  public ResponseEntity<AppointmentDTO> create(@Valid @RequestBody AppointmentDTO dto) {
    AppointmentDTO created = appointmentService.createAppointment(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','USER')")
  public ResponseEntity<AppointmentDTO> update(@PathVariable Long id, @RequestBody AppointmentDTO dto) {
    return ResponseEntity.ok(appointmentService.updateAppointment(id, dto));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    appointmentService.deleteAppointment(id);
    return ResponseEntity.noContent().build();
  }
}
