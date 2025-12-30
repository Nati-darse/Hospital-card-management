package com.hospital.card.components;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.card.entity.User;
import com.hospital.card.entity.UserRole;
import com.hospital.card.repository.AppointmentRepository;
import com.hospital.card.repository.MedicalVisitRepository;
import com.hospital.card.repository.PatientRepository;
import com.hospital.card.repository.StaffRepository;
import com.hospital.card.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final PatientRepository patientRepository;
    private final MedicalVisitRepository medicalVisitRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Cleanup all data
        medicalVisitRepository.deleteAll();
        appointmentRepository.deleteAll();
        staffRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();

        // Create Default Admin
        createAdminUser();
    }

    private void createAdminUser() {
        if (userRepository.findByUsername("Nati").isEmpty()) {
            User admin = new User();
            admin.setUsername("Nati");
            admin.setEmail("nati@atlashospital.com"); // Dummy email
            admin.setPassword(passwordEncoder.encode("nati123"));
            admin.setRole(UserRole.ADMIN);
            admin.setFirstName("Nati");
            admin.setLastName("Admin");
            admin.setIsActive(true);
            userRepository.save(admin);
            System.out.println("Default Admin 'Nati' created.");
        }
    }
}
