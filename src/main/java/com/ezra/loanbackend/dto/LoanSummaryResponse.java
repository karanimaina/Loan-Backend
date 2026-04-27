package com.ezra.loanbackend.dto;

import com.ezra.loanbackend.constants.LoanState;

import java.math.BigDecimal;

public record LoanSummaryResponse(
        Long loanId,
        Long productId,
        LoanState state,
        BigDecimal outstandingBalance,
        java.time.LocalDate dueDate
) {
}
