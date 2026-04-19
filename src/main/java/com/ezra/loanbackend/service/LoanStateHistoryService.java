package com.ezra.loanbackend.service;

import com.ezra.loanbackend.domain.Loan;
import com.ezra.loanbackend.domain.LoanState;
import com.ezra.loanbackend.domain.LoanStateHistory;
import com.ezra.loanbackend.repository.LoanStateHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LoanStateHistoryService {

    private final LoanStateHistoryRepository historyRepository;

    @Transactional
    public void record(Loan loan, LoanState state, Integer installmentNumber) {
        LoanStateHistory row = LoanStateHistory.builder()
                .loan(loan)
                .installmentNumber(installmentNumber)
                .amount(loan.getPrincipalAmount())
                .outstandingAmount(loan.getOutstandingBalance())
                .dueDate(loan.getDueDate())
                .state(state)
                .recordedAt(Instant.now())
                .build();
        historyRepository.save(row);
    }
}
