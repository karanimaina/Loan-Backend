package com.ezra.loanbackend.web.dto;

import com.ezra.loanbackend.domain.BillingCycleType;
import com.ezra.loanbackend.domain.LoanState;
import com.ezra.loanbackend.domain.LoanStructure;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class LoanDtos {

    private LoanDtos() {
    }

    public record OriginateLoanRequest(
            @NotNull Long customerId,
            @NotNull Long productId,
            @NotNull @Positive BigDecimal principalAmount,
            @NotNull LoanStructure loanStructure,
            @NotNull BillingCycleType billingCycleType,
            @NotNull LocalDate disbursementDate,
            Long consolidatedBillingGroupId,
            LocalDate newConsolidatedGroupNextDueDate,
            String consolidatedGroupLabel
    ) {
    }

    public record CancelLoanRequest(
            String reason
    ) {
    }

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
}
