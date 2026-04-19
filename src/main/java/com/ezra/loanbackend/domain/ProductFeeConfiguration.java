package com.ezra.loanbackend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    /** Service fee: fixed amount (optional). */
    @Column(precision = 19, scale = 4)
    private BigDecimal serviceFeeFixed;

    /** Service fee: percentage of principal (optional, e.g. 2.5 = 2.5%). */
    @Column(precision = 19, scale = 4)
    private BigDecimal serviceFeePercent;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private ServiceFeeTiming serviceFeeTiming;

    /** Daily fee rate applied to outstanding balance per day (optional). */
    @Column(precision = 19, scale = 4)
    private BigDecimal dailyFeeRateOnBalance;

    /** Free-text / flag for when daily fee applies (e.g. after day 30). */
    @Column(length = 500)
    private String dailyFeeConditions;

    /** Late fee fixed amount. */
    @Column(precision = 19, scale = 4)
    private BigDecimal lateFeeFixed;

    /** Late fee as percent of overdue amount. */
    @Column(precision = 19, scale = 4)
    private BigDecimal lateFeePercent;

    /**
     * Days after scheduled due date before late fee triggers (per product).
     */
    private Integer daysAfterDueBeforeLateFee;

    /**
     * Generic grace / policy field: days after due before various fees apply.
     */
    private Integer daysAfterDueForFeeApplication;
}
