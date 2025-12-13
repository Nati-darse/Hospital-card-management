package com.hospital.card.config;

import com.hospital.card.entity.User;
import com.hospital.card.entity.UserRole;
import com.hospital.card.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) throws Exception {
    //create Admin with my name and pasword 
        String adminUsername = "Nati";
        if (userService.findByUsername(adminUsername).isEmpty()) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail("nati@hospital.local");
            admin.setPassword("nati123");
            admin.setRole(UserRole.ADMIN);
            admin.setFirstName("Nati");
            admin.setLastName("Admin");
            admin.setPhoneNumber("0000000000");
            admin.setDateOfBirth(LocalDate.of(1990,1,1));
            admin.setGender("Other");
            admin.setAddress("System");
            userService.registerUser(admin);
            System.out.println("Created default admin user: 'Nati' with password 'nati123'");
        }
    }
}
