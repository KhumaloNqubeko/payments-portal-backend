package com.bank.paymentsportal.config;

import com.bank.paymentsportal.entity.User;
import com.bank.paymentsportal.entity.UserRole;
import com.bank.paymentsportal.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            seedIfMissing(userRepository, passwordEncoder,
                    "Nokwandisa Khumalo", "nonhlekhumzie", "nokwandisa.khumalo@secure.com", "9001015800081", "12345678", "Cust0mer!Pass1", UserRole.CUSTOMER);
            seedIfMissing(userRepository, passwordEncoder,
                    "Bonginkosi Tlou", "bob", "bob@secure.com", "9102025800082", "123456789", "Cust0mer!Pass2", UserRole.CUSTOMER);
            seedIfMissing(userRepository, passwordEncoder,
                    "Nokwanda Khumalo", "nonokhumzie", "nokwanda.khumalo@secure.com", "8803035800083", "87654321", "Employ3e!Pass1", UserRole.EMPLOYEE);
        };
    }

    private void seedIfMissing(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               String fullName,
                               String username,
                               String email,
                               String southAfricanIdNumber,
                               String accountNumber,
                               String password,
                               UserRole role) {
        if (!userRepository.existsByUsername(username)) {
            userRepository.save(User.builder()
                    .fullName(fullName)
                    .username(username)
                    .email(email)
                    .southAfricanIdNumber(southAfricanIdNumber)
                    .accountNumber(accountNumber)
                    .passwordHash(passwordEncoder.encode(password))
                    .role(role)
                    .build());
        }
    }
}
