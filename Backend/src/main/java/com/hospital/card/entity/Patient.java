package com.hospital.card.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "medical_record_number", unique = true)
    private String medicalRecordNumber;

    @Column(name = "blood_group")
    private String bloodGroup;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    private String allergies;

    @Column(name = "chronic_conditions")
    private String chronicConditions;

    @Column(name = "insurance_provider")
    private String insuranceProvider;

    @Column(name = "insurance_number")
    private String insuranceNumber;

    @ManyToOne
    @JoinColumn(name = "assigned_doctor_id")
    private Staff assignedDoctor;
}
