package com.ezra.loanbackend.service.schedule;

import com.ezra.loanbackend.domain.OriginatedProduct;
import com.ezra.loanbackend.constants.TenureType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LoanScheduleCalculatorTest {

    @Test
    void maturityDate_addsDays() {
        OriginatedProduct p = OriginatedProduct.builder()
                .tenureType(TenureType.DAYS)
                .tenureValue(30)
                .fixedTerm(true)
                .build();
        LocalDate d = LocalDate.of(2026, 1, 1);
        assertThat(LoanScheduleCalculator.maturityDate(d, p)).isEqualTo(LocalDate.of(2026, 1, 31));
    }

    @Test
    void equalMonthlyInstallments_splitsPrincipal() {
        OriginatedProduct p = OriginatedProduct.builder()
                .tenureType(TenureType.MONTHS)
                .tenureValue(12)
                .fixedTerm(true)
                .numberOfInstallments(12)
                .build();
        LocalDate disb = LocalDate.of(2026, 1, 15);
        var drafts = LoanScheduleCalculator.equalMonthlyInstallments(disb, new BigDecimal("1200.00"), p);
        assertThat(drafts).hasSize(12);
        assertThat(drafts.get(0).dueDate()).isEqualTo(LocalDate.of(2026, 2, 15));
        BigDecimal sum = drafts.stream()
                .map(LoanScheduleCalculator.InstallmentDraft::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(sum).isEqualByComparingTo(new BigDecimal("1200.00"));
    }
}
