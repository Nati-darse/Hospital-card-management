package com.hospital.card.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffDTO {
    private Long id;
    private Long userId;
    private UserDTO user;
    private String staffId;
    private String department;
    private String specialization;
    private String qualification;
    private LocalDate joiningDate;
}
