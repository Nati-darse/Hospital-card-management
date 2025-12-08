package com.hospital.card.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "hospital_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HospitalCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    private Patient patient;

    @Column(name = "card_number", unique = true)
    private String cardNumber;

    @Column(name = "qr_code_data", columnDefinition = "text")
    private String qrCodeData;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "status")
    private String status; // ACTIVE/INACTIVE/EXPIRED
}
