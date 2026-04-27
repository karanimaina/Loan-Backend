package com.ezra.loanbackend.scheduler;

import com.ezra.loanbackend.constants.InstallmentState;
import com.ezra.loanbackend.domain.Loan;
import com.ezra.loanbackend.domain.LoanInstallment;
import com.ezra.loanbackend.constants.LoanState;
import com.ezra.loanbackend.constants.LoanStructure;
import com.ezra.loanbackend.constants.NotificationEventType;
import com.ezra.loanbackend.notification.LoanNotificationPublisher;
import com.ezra.loanbackend.repository.LoanInstallmentRepository;
import com.ezra.loanbackend.repository.LoanRepository;
import com.ezra.loanbackend.service.FeeService;
import com.ezra.loanbackend.service.LoanStateHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Background sweeps: overdue (daily), daily fee accrual, write-off (weekly).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoanSweepScheduler {

    private final LoanRepository loanRepository;
    private final LoanInstallmentRepository installmentRepository;
    private final FeeService feeService;
    private final LoanStateHistoryService historyService;
    private final LoanNotificationPublisher notificationPublisher;

    @Value("${loan.sweep.writeoff-days-after-due:120}")
    private int writeoffDaysAfterDue;

    @Scheduled(cron = "${loan.sweep.overdue-cron:0 0 2 * * *}")
    @Transactional
    public void overdueSweep() {
        LocalDate today = LocalDate.now();
        Set<Long> seen = new HashSet<>();
        List<Loan> pastDue = loanRepository.findDueBefore(List.of(LoanState.OPEN), today);
        for (Loan loan : pastDue) {
            if (seen.add(loan.getId())) {
                markOverdue(loan, today);
            }
        }
        for (Loan loan : installmentRepository.findLoansWithOverdueInstallments(LoanState.OPEN, today)) {
            if (seen.add(loan.getId())) {
                markOverdue(loan, today);
            }
        }
    }

    private void markOverdue(Loan loan, LocalDate today) {
        loan.setState(LoanState.OVERDUE);
        loan.setUpdatedAt(Instant.now());
        for (LoanInstallment ins : installmentRepository.findByLoanIdOrderByInstallmentNumber(loan.getId())) {
            if (ins.getDueDate().isBefore(today) && ins.getOutstandingAmount().signum() > 0) {
                ins.setState(InstallmentState.OVERDUE);
                installmentRepository.save(ins);
            }
        }
        historyService.record(loan, LoanState.OVERDUE, null);
        feeService.applyLateFeeIfDue(loan, today);
        loanRepository.save(loan);
        notificationPublisher.publish(NotificationEventType.OVERDUE_NOTICE, loan.getCustomerId(), loan);
    }

    @Scheduled(cron = "${loan.sweep.daily-fee-cron:0 15 2 * * *}")
    @Transactional
    public void dailyFeeAccrualSweep() {
        LocalDate today = LocalDate.now();
        List<Loan> active = loanRepository.findAll().stream()
                .filter(l -> l.getState() == LoanState.OPEN || l.getState() == LoanState.OVERDUE)
                .filter(l -> l.getLoanStructure() == LoanStructure.LUMP_SUM)
                .toList();
        for (Loan loan : active) {
            feeService.applyDailyFeeForDay(loan, today);
            loan.setUpdatedAt(Instant.now());
            loanRepository.save(loan);
        }
    }

    @Scheduled(cron = "${loan.sweep.writeoff-cron:0 30 3 * * SUN}")
    @Transactional
    public void writeOffSweep() {
        LocalDate cutoff = LocalDate.now().minusDays(writeoffDaysAfterDue);
        List<Loan> candidates = loanRepository.findByStateAndDueDateBefore(LoanState.OVERDUE, cutoff);
        for (Loan loan : candidates) {
            loan.setState(LoanState.WRITTEN_OFF);
            loan.setUpdatedAt(Instant.now());
            historyService.record(loan, LoanState.WRITTEN_OFF, null);
            loanRepository.save(loan);
            notificationPublisher.publish(NotificationEventType.WRITE_OFF, loan.getCustomerId(), loan);
        }
    }
}
