package com.hospital.card.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private String title;
    private String message;
    private String type; // referral, appointment, prescription, etc.
    private Long entityId; // Reference to related entity (referral_id, appointment_id, etc.)
    
    private Boolean isRead = false;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
