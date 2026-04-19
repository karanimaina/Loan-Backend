package com.ezra.loanbackend.service;

import com.ezra.loanbackend.domain.AllocationType;
import com.ezra.loanbackend.domain.InstallmentState;
import com.ezra.loanbackend.domain.Loan;
import com.ezra.loanbackend.domain.LoanInstallment;
import com.ezra.loanbackend.domain.LoanRepayment;
import com.ezra.loanbackend.domain.LoanState;
import com.ezra.loanbackend.domain.LoanStructure;
import com.ezra.loanbackend.domain.NotificationEventType;
import com.ezra.loanbackend.domain.RepaymentAllocation;
import com.ezra.loanbackend.domain.RepaymentChannel;
import com.ezra.loanbackend.notification.LoanNotificationPublisher;
import com.ezra.loanbackend.repository.LoanInstallmentRepository;
import com.ezra.loanbackend.repository.LoanRepaymentRepository;
import com.ezra.loanbackend.repository.LoanRepository;
import com.ezra.loanbackend.repository.RepaymentAllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepaymentService {

    private final LoanRepository loanRepository;
    private final LoanRepaymentRepository repaymentRepository;
    private final RepaymentAllocationRepository allocationRepository;
    private final LoanInstallmentRepository installmentRepository;
    private final LoanStateHistoryService historyService;
    private final LoanNotificationPublisher notificationPublisher;

    @Transactional
    public LoanRepayment recordRepayment(
            Long loanId,
            BigDecimal amount,
            LocalDate paymentDate,
            RepaymentChannel channel,
            String externalReference) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found: " + loanId));
        if (loan.getState() != LoanState.OPEN && loan.getState() != LoanState.OVERDUE) {
            throw new IllegalStateException("Loan is not repayable in state " + loan.getState());
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        LoanRepayment repayment = LoanRepayment.builder()
                .loan(loan)
                .amount(amount)
                .paymentDate(paymentDate)
                .channel(channel)
                .externalReference(externalReference)
                .createdAt(Instant.now())
                .build();
        repayment = repaymentRepository.save(repayment);

        BigDecimal remaining = amount;
        if (loan.getLoanStructure() == LoanStructure.INSTALLMENT) {
            List<LoanInstallment> installments = installmentRepository.findByLoanIdOrderByInstallmentNumber(loan.getId());
            installments.sort(Comparator.comparing(LoanInstallment::getDueDate));
            for (LoanInstallment ins : installments) {
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
                if (ins.getOutstandingAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                BigDecimal pay = remaining.min(ins.getOutstandingAmount());
                ins.setOutstandingAmount(ins.getOutstandingAmount().subtract(pay));
                if (ins.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0) {
                    ins.setPaidDate(paymentDate);
                    ins.setState(InstallmentState.PAID);
                } else {
                    ins.setState(InstallmentState.PARTIALLY_PAID);
                }
                remaining = remaining.subtract(pay);
                allocationRepository.save(RepaymentAllocation.builder()
                        .loan(loan)
                        .repayment(repayment)
                        .installment(ins)
                        .amountAllocated(pay)
                        .allocationType(AllocationType.PRINCIPAL)
                        .allocatedAt(Instant.now())
                        .build());
                installmentRepository.save(ins);
            }
        } else {
            BigDecimal pay = remaining.min(loan.getOutstandingBalance());
            loan.setOutstandingBalance(loan.getOutstandingBalance().subtract(pay));
            remaining = remaining.subtract(pay);
            allocationRepository.save(RepaymentAllocation.builder()
                    .loan(loan)
                    .repayment(repayment)
                    .installment(null)
                    .amountAllocated(pay)
                    .allocationType(AllocationType.PRINCIPAL)
                    .allocatedAt(Instant.now())
                    .build());
        }

        if (loan.getLoanStructure() == LoanStructure.INSTALLMENT) {
            BigDecimal totalInstOut = installmentRepository.findByLoanIdOrderByInstallmentNumber(loan.getId()).stream()
                    .map(LoanInstallment::getOutstandingAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            loan.setOutstandingBalance(totalInstOut);
            refreshNextDueDate(loan);
        }

        Long customerId = loan.getCustomerId();
        if (loan.getOutstandingBalance().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setState(LoanState.CLOSED);
            historyService.record(loan, LoanState.CLOSED, null);
            notificationPublisher.publish(NotificationEventType.LOAN_CLOSED, customerId, loan);
        } else {
            notificationPublisher.publish(NotificationEventType.REPAYMENT_RECEIVED, customerId, loan);
        }
        loan.setUpdatedAt(Instant.now());
        loanRepository.save(loan);
        return repayment;
    }

    private void refreshNextDueDate(Loan loan) {
        installmentRepository.findByLoanIdOrderByInstallmentNumber(loan.getId()).stream()
                .filter(i -> i.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0)
                .min(Comparator.comparing(LoanInstallment::getDueDate))
                .ifPresentOrElse(
                        next -> loan.setDueDate(next.getDueDate()),
                        () -> loan.setDueDate(null));
    }
}
