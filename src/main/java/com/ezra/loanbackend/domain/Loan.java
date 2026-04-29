package com.ezra.loanbackend.domain;

import com.ezra.loanbackend.constants.BillingCycleType;
import com.ezra.loanbackend.constants.LoanState;
import com.ezra.loanbackend.constants.LoanStructure;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Embedded
    private OriginatedProduct originatedProduct;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal principalAmount;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal outstandingBalance;

    private LocalDate disbursementDate;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private LoanStructure loanStructure;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private BillingCycleType billingCycleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private LoanState state;

    @Column(length = 1000)
    private String cancellationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consolidated_billing_group_id")
    private ConsolidatedBillingGroup consolidatedBillingGroup;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
    private LocalDate lastDailyFeeAccrualDate;
}
