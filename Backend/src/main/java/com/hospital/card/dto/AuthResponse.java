package com.hospital.card.dto;

import com.hospital.card.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private UserRole role;
    private String firstName;
    private String lastName;
    private String message;
}