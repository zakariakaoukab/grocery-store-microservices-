package com.epicerie.auth.config;

import com.epicerie.auth.domain.Role;
import com.epicerie.auth.domain.User;
import com.epicerie.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Seeding initial users in auth-service...");

            User admin = User.builder()
                    .username("admin")
                    .email("admin@epicerie.com")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Set.of(Role.ADMIN))
                    .active(true)
                    .build();

            User customer = User.builder()
                    .username("customer1")
                    .email("customer1@epicerie.com")
                    .password(passwordEncoder.encode("password123"))
                    .roles(Set.of(Role.CUSTOMER))
                    .active(true)
                    .build();

            User manager = User.builder()
                    .username("manager1")
                    .email("manager1@epicerie.com")
                    .password(passwordEncoder.encode("manager123"))
                    .roles(Set.of(Role.MANAGER))
                    .active(true)
                    .build();

            userRepository.save(admin);
            userRepository.save(customer);
            userRepository.save(manager);

            log.info("Finished seeding initial users.");
        }
    }
}
