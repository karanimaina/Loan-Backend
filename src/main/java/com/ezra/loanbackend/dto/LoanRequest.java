package com.ezra.loanbackend.dto;

import com.ezra.loanbackend.constants.BillingCycleType;
import com.ezra.loanbackend.constants.LoanStructure;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanRequest(
        @NotNull Long customerId,
        @NotNull Long productId,
        @NotNull @Positive BigDecimal principalAmount,
            @NotNull String loanStructure,
        @NotNull String billingCycleType,
        @NotNull LocalDate disbursementDate,
        Long consolidatedBillingGroupId,
        LocalDate newConsolidatedGroupNextDueDate,
        String consolidatedGroupLabel
) {
}
