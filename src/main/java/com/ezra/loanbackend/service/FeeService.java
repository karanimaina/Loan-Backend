package com.ezra.loanbackend.service;

import com.ezra.loanbackend.domain.FeeTrigger;
import com.ezra.loanbackend.domain.FeeType;
import com.ezra.loanbackend.domain.Loan;
import com.ezra.loanbackend.domain.LoanFeeCharge;
import com.ezra.loanbackend.domain.LoanInstallment;
import com.ezra.loanbackend.domain.LoanStructure;
import com.ezra.loanbackend.domain.ProductFeeConfiguration;
import com.ezra.loanbackend.domain.ServiceFeeTiming;
import com.ezra.loanbackend.repository.LoanFeeChargeRepository;
import com.ezra.loanbackend.repository.LoanInstallmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FeeService {

    private final LoanFeeChargeRepository feeChargeRepository;
    private final LoanInstallmentRepository installmentRepository;

    @Transactional
    public LoanFeeCharge applyServiceFeeAtOrigination(Loan loan, LocalDate asOf) {
        ProductFeeConfiguration cfg = feeConfig(loan);
        if (cfg == null || cfg.getServiceFeeTiming() != ServiceFeeTiming.AT_ORIGINATION) {
            return null;
        }
        BigDecimal fee = computeServiceFeeAmount(loan.getPrincipalAmount(), cfg);
        if (fee.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        if (loan.getLoanStructure() == LoanStructure.INSTALLMENT) {
            LoanInstallment first = installmentRepository.findByLoanIdOrderByInstallmentNumber(loan.getId()).get(0);
            first.setAmount(first.getAmount().add(fee));
            first.setOutstandingAmount(first.getOutstandingAmount().add(fee));
            installmentRepository.save(first);
        } else {
            loan.setOutstandingBalance(loan.getOutstandingBalance().add(fee));
        }
        LoanFeeCharge charge = LoanFeeCharge.builder()
                .loan(loan)
                .feeType(FeeType.SERVICE)
                .amount(fee)
                .appliedDate(asOf)
                .trigger(FeeTrigger.ORIGINATION)
                .description("Service fee at origination")
                .createdAt(Instant.now())
                .build();
        return feeChargeRepository.save(charge);
    }

    @Transactional
    public LoanFeeCharge applyDailyFeeForDay(Loan loan, LocalDate day) {
        ProductFeeConfiguration cfg = feeConfig(loan);
        if (cfg == null || cfg.getDailyFeeRateOnBalance() == null) {
            return null;
        }
        if (day.equals(loan.getLastDailyFeeAccrualDate())) {
            return null;
        }
        if (loan.getOutstandingBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        BigDecimal rate = cfg.getDailyFeeRateOnBalance();
        BigDecimal fee = loan.getOutstandingBalance().multiply(rate).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        if (fee.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        loan.setOutstandingBalance(loan.getOutstandingBalance().add(fee));
        loan.setLastDailyFeeAccrualDate(day);
        LoanFeeCharge charge = LoanFeeCharge.builder()
                .loan(loan)
                .feeType(FeeType.DAILY)
                .amount(fee)
                .appliedDate(day)
                .trigger(FeeTrigger.DAILY_ACCRUAL)
                .description("Daily fee on balance")
                .createdAt(Instant.now())
                .build();
        return feeChargeRepository.save(charge);
    }

    /**
     * Late fee when days past due exceeds configured grace (per product snapshot).
     */
    @Transactional
    public LoanFeeCharge applyLateFeeIfDue(Loan loan, LocalDate today) {
        ProductFeeConfiguration cfg = feeConfig(loan);
        if (cfg == null) {
            return null;
        }
        Integer grace = cfg.getDaysAfterDueBeforeLateFee() != null
                ? cfg.getDaysAfterDueBeforeLateFee()
                : cfg.getDaysAfterDueForFeeApplication();
        if (grace == null || loan.getDueDate() == null) {
            return null;
        }
        LocalDate lateStarts = loan.getDueDate().plusDays(grace);
        if (today.isBefore(lateStarts)) {
            return null;
        }
        if (feeChargeRepository.existsByLoanIdAndFeeTypeAndAppliedDate(loan.getId(), FeeType.LATE, today)) {
            return null;
        }
        BigDecimal base = loan.getOutstandingBalance();
        BigDecimal fee = BigDecimal.ZERO;
        if (cfg.getLateFeeFixed() != null) {
            fee = fee.add(cfg.getLateFeeFixed());
        }
        if (cfg.getLateFeePercent() != null) {
            fee = fee.add(base.multiply(cfg.getLateFeePercent()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        }
        if (fee.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        loan.setOutstandingBalance(loan.getOutstandingBalance().add(fee));
        LoanFeeCharge charge = LoanFeeCharge.builder()
                .loan(loan)
                .feeType(FeeType.LATE)
                .amount(fee)
                .appliedDate(today)
                .trigger(FeeTrigger.LATE_PAYMENT)
                .description("Late fee")
                .createdAt(Instant.now())
                .build();
        return feeChargeRepository.save(charge);
    }

    private static ProductFeeConfiguration feeConfig(Loan loan) {
        if (loan.getOriginatedProductTerms() == null) {
            return null;
        }
        return loan.getOriginatedProductTerms().getFeeConfiguration();
    }

    private static BigDecimal computeServiceFeeAmount(BigDecimal principal, ProductFeeConfiguration cfg) {
        BigDecimal fee = BigDecimal.ZERO;
        if (cfg.getServiceFeeFixed() != null) {
            fee = fee.add(cfg.getServiceFeeFixed());
        }
        if (cfg.getServiceFeePercent() != null) {
            fee = fee.add(principal.multiply(cfg.getServiceFeePercent()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        }
        return fee;
    }
}
