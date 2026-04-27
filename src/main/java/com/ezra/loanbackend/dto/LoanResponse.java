package com.ezra.loanbackend.dto;

import com.ezra.loanbackend.constants.BillingCycleType;
import com.ezra.loanbackend.constants.LoanState;
import com.ezra.loanbackend.constants.LoanStructure;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanResponse(
        Long id,
        Long customerId,
        Long productId,
        BigDecimal principalAmount,
        BigDecimal outstandingBalance,
        LocalDate disbursementDate,
        LocalDate dueDate,
        LoanStructure loanStructure,
        BillingCycleType billingCycleType,
        LoanState state,
        String cancellationReason,
        Long consolidatedBillingGroupId
) {
}