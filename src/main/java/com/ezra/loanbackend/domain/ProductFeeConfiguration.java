package com.ezra.loanbackend.domain;

import com.ezra.loanbackend.constants.ServiceFeeTiming;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.math.BigDecimal;

/**
 * Fee configuration for a loan product (service, daily, late) per case study.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFeeConfiguration {
    @Column(precision = 19, scale = 4)
    private BigDecimal serviceFeeFixed;

    @Column(precision = 19, scale = 4)
    private BigDecimal serviceFeePercent;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private ServiceFeeTiming serviceFeeTiming;

    @Column(precision = 19, scale = 4)
    private BigDecimal dailyFeeRateOnBalance;

    @Column(length = 500)
    private String dailyFeeConditions;

    @Column(precision = 19, scale = 4)
    private BigDecimal lateFeeFixed;

    @Column(precision = 19, scale = 4)
    private BigDecimal lateFeePercent;
    private Integer daysAfterDueBeforeLateFee;
    private Integer daysAfterDueForFeeApplication;
}
