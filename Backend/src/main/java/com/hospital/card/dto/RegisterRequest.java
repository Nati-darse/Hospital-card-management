package com.hospital.card.dto;

import java.time.LocalDate;

import com.hospital.card.entity.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @jakarta.validation.constraints.NotBlank(message = "Username is required")
    private String username;
    
    @jakarta.validation.constraints.NotBlank(message = "Email is required")
    private String email;
    
    @jakarta.validation.constraints.NotBlank(message = "Password is required")
    private String password;
    
    @jakarta.validation.constraints.NotNull(message = "Role is required")
    private UserRole role;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String department;
}

// dto =data transfer objects yemiyamelekt new