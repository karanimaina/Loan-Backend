package com.ezra.loanbackend.dto;

import com.ezra.loanbackend.constants.RepaymentChannel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class RepaymentDtos {

    private RepaymentDtos() {
    }

    public record RecordRepaymentRequest(
            @NotNull @Positive BigDecimal amount,
            @NotNull LocalDate paymentDate,
            @NotNull RepaymentChannel channel,
            String externalReference
    ) {
    }

    public record RepaymentResponse(
            Long id,
            Long loanId,
            BigDecimal amount,
            LocalDate paymentDate,
            RepaymentChannel channel,
            String externalReference
    ) {
    }
}
