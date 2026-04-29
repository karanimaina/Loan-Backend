package com.ezra.loanbackend.service;

import com.ezra.loanbackend.constants.InstallmentState;
import com.ezra.loanbackend.domain.Loan;
import com.ezra.loanbackend.domain.LoanInstallment;
import com.ezra.loanbackend.constants.LoanStructure;
import com.ezra.loanbackend.repository.LoanInstallmentRepository;
import com.ezra.loanbackend.service.schedule.LoanScheduleCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstallmentService {

    private final LoanInstallmentRepository installmentRepository;

    @Transactional
    public List<LoanInstallment> createScheduleIfNeeded(Loan loan) {
        if (loan.getLoanStructure() != LoanStructure.INSTALLMENT) {
            return List.of();
        }
        var terms = loan.getOriginatedProduct();
        List<LoanScheduleCalculator.InstallmentDraft> drafts = LoanScheduleCalculator.equalMonthlyInstallments(
                loan.getDisbursementDate(),
                loan.getPrincipalAmount(),
                terms);
        return drafts.stream().map(d -> LoanInstallment.builder()
                .loan(loan)
                .installmentNumber(d.number())
                .amount(d.amount())
                .outstandingAmount(d.amount())
                .dueDate(d.dueDate())
                .state(InstallmentState.PENDING)
                .build()).map(installmentRepository::save).toList();
    }
}
