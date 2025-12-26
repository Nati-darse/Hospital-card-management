package com.hospital.card.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Entity
@Table(name = "medical_visits")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Staff doctor;

    @Column(name = "visit_date")
    private LocalDate visitDate;

    private String diagnosis;

    private String prescription;

    @Column(name = "lab_tests")
    private String labTests;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "additional_comments", columnDefinition = "TEXT")
    private String additionalComments;

    @CreationTimestamp
    private java.time.LocalDateTime createdAt;
}
