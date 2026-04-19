package com.ezra.loanbackend.repository;

import com.ezra.loanbackend.domain.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, Long> {
    List<LoanRepayment> findByLoanIdOrderByCreatedAtDesc(Long loanId);
}
