package com.hospital.card.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminPasswordResetRequest {
    @NotBlank(message = "New password is required")
    private String newPassword;
}
