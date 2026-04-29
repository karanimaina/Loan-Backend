package com.ezra.loanbackend.service.schedule;

import com.ezra.loanbackend.domain.OriginatedProduct;
import com.ezra.loanbackend.constants.TenureType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class LoanScheduleCalculator {

    private LoanScheduleCalculator() {
    }

    public static LocalDate maturityDate(LocalDate disbursement, OriginatedProduct terms) {
        if (terms.getTenureType() == TenureType.DAYS) {
            return disbursement.plusDays(terms.getTenureValue());
        }
        return disbursement.plusMonths(terms.getTenureValue());
    }

    public static int installmentCount(OriginatedProduct terms) {
        if (terms.getNumberOfInstallments() != null && terms.getNumberOfInstallments() > 0) {
            return terms.getNumberOfInstallments();
        }
        if (terms.getTenureType() == TenureType.MONTHS) {
            return terms.getTenureValue();
        }
        return Math.max(1, terms.getTenureValue());
    }

    public static List<InstallmentDraft> equalMonthlyInstallments(
            LocalDate disbursement,
            BigDecimal principal,
            OriginatedProduct terms) {
        int n = installmentCount(terms);
        BigDecimal each = principal.divide(BigDecimal.valueOf(n), 4, RoundingMode.HALF_UP);
        List<InstallmentDraft> drafts = new ArrayList<>();
        BigDecimal allocated = BigDecimal.ZERO;
        for (int i = 1; i <= n; i++) {
            LocalDate due = disbursement.plusMonths(i);
            BigDecimal amt = (i == n) ? principal.subtract(allocated) : each;
            allocated = allocated.add(amt);
            drafts.add(new InstallmentDraft(i, amt, due));
        }
        return drafts;
    }

    public record InstallmentDraft(int number, BigDecimal amount, LocalDate dueDate) {
    }
}
