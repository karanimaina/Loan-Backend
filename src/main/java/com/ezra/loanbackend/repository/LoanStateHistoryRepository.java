package com.ezra.loanbackend.repository;

import com.ezra.loanbackend.domain.LoanStateHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanStateHistoryRepository extends JpaRepository<LoanStateHistory, Long> {
    List<LoanStateHistory> findByLoanIdOrderByRecordedAtDesc(Long loanId);
}
