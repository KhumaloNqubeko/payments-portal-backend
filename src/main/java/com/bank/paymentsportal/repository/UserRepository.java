package com.bank.paymentsportal.repository;

import com.bank.paymentsportal.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByAccountNumber(String accountNumber);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByAccountNumber(String accountNumber);

    boolean existsBySouthAfricanIdNumber(String southAfricanIdNumber);
}
