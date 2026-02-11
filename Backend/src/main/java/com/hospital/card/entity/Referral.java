package com.hospital.card.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "referrals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Referral {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;
    
    @ManyToOne
    @JoinColumn(name = "referring_doctor_id")
    private Staff referringDoctor;
    
    @ManyToOne
    @JoinColumn(name = "referred_doctor_id")
    private Staff referredDoctor;
    
    private String department;
    private String reason;
    private String status; // Pending, Accepted, Rejected, Completed
    private LocalDate referralDate;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
