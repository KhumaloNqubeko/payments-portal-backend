package com.bank.paymentsportal.repository;

import com.bank.paymentsportal.entity.PaymentTransaction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    @EntityGraph(attributePaths = {"customer", "verifiedBy"})
    List<PaymentTransaction> findAllByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @EntityGraph(attributePaths = {"customer", "verifiedBy"})
    List<PaymentTransaction> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"customer", "verifiedBy"})
    Optional<PaymentTransaction> findByIdAndCustomerId(Long id, Long customerId);

    @Override
    @EntityGraph(attributePaths = {"customer", "verifiedBy"})
    Optional<PaymentTransaction> findById(Long id);
}
