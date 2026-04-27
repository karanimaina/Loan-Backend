package com.ezra.loanbackend.domain;

import com.ezra.loanbackend.constants.FeeTrigger;
import com.ezra.loanbackend.constants.FeeType;
import jakarta.persistence.Column;
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
@Table(name = "loan_fee_charges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanFeeCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private FeeType feeType;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate appliedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FeeTrigger trigger;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Instant createdAt;
}
