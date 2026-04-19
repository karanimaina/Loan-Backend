package com.ezra.loanbackend.service.schedule;

import com.ezra.loanbackend.domain.OriginatedProductTerms;
import com.ezra.loanbackend.domain.TenureType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LoanScheduleCalculatorTest {

    @Test
    void maturityDate_addsDays() {
        OriginatedProductTerms p = OriginatedProductTerms.builder()
                .tenureType(TenureType.DAYS)
                .tenureValue(30)
                .fixedTerm(true)
                .build();
        LocalDate d = LocalDate.of(2026, 1, 1);
        assertThat(LoanScheduleCalculator.maturityDate(d, p)).isEqualTo(LocalDate.of(2026, 1, 31));
    }

    @Test
    void equalMonthlyInstallments_splitsPrincipal() {
        OriginatedProductTerms p = OriginatedProductTerms.builder()
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
