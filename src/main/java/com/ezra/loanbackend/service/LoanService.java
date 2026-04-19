package com.ezra.loanbackend.service;

import com.ezra.loanbackend.domain.BillingCycleType;
import com.ezra.loanbackend.domain.ConsolidatedBillingGroup;
import com.ezra.loanbackend.domain.Loan;
import com.ezra.loanbackend.domain.LoanInstallment;
import com.ezra.loanbackend.domain.LoanState;
import com.ezra.loanbackend.domain.LoanStructure;
import com.ezra.loanbackend.domain.NotificationEventType;
import com.ezra.loanbackend.domain.OriginatedProductTerms;
import com.ezra.loanbackend.integration.customer.CustomerServiceGateway;
import com.ezra.loanbackend.integration.customer.RemoteCustomerDto;
import com.ezra.loanbackend.integration.product.ProductServiceGateway;
import com.ezra.loanbackend.integration.product.ProductTermsMapper;
import com.ezra.loanbackend.integration.product.RemoteLoanProductDto;
import com.ezra.loanbackend.notification.LoanNotificationPublisher;
import com.ezra.loanbackend.repository.ConsolidatedBillingGroupRepository;
import com.ezra.loanbackend.repository.LoanInstallmentRepository;
import com.ezra.loanbackend.repository.LoanRepository;
import com.ezra.loanbackend.service.schedule.LoanScheduleCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final ConsolidatedBillingGroupRepository billingGroupRepository;
    private final LoanInstallmentRepository installmentRepository;
    private final InstallmentService installmentService;
    private final FeeService feeService;
    private final LoanStateHistoryService historyService;
    private final LoanNotificationPublisher notificationPublisher;
    private final CustomerServiceGateway customerServiceGateway;
    private final ProductServiceGateway productServiceGateway;

    @Transactional
    public Loan originate(
            Long customerId,
            Long productId,
            BigDecimal principalAmount,
            LoanStructure loanStructure,
            BillingCycleType billingCycleType,
            LocalDate disbursementDate,
            Long consolidatedBillingGroupId,
            LocalDate newGroupNextDueDate,
            String consolidatedGroupLabel) {
        RemoteCustomerDto customer = customerServiceGateway.getCustomer(customerId);
        RemoteLoanProductDto product = productServiceGateway.getProduct(productId);
        if (!product.active()) {
            throw new IllegalStateException("Product is inactive");
        }

        BigDecimal current = loanRepository.sumOutstandingForCustomer(customerId,
                List.of(LoanState.OPEN, LoanState.OVERDUE));
        if (current.add(principalAmount).compareTo(customer.loanLimitAmount()) > 0) {
            throw new IllegalStateException("Principal exceeds customer loan limit");
        }

        OriginatedProductTerms terms = ProductTermsMapper.fromRemote(product);

        ConsolidatedBillingGroup group = null;
        if (billingCycleType == BillingCycleType.CONSOLIDATED) {
            if (consolidatedBillingGroupId != null) {
                group = billingGroupRepository.findById(consolidatedBillingGroupId)
                        .orElseThrow(() -> new IllegalArgumentException("Billing group not found"));
                if (!group.getCustomerId().equals(customerId)) {
                    throw new IllegalStateException("Billing group does not belong to this customer");
                }
            } else if (newGroupNextDueDate != null) {
                group = billingGroupRepository.save(ConsolidatedBillingGroup.builder()
                        .customerId(customerId)
                        .label(consolidatedGroupLabel != null ? consolidatedGroupLabel : "Consolidated")
                        .nextDueDate(newGroupNextDueDate)
                        .createdAt(Instant.now())
                        .build());
            } else {
                throw new IllegalArgumentException("Consolidated billing requires a group id or newGroupNextDueDate");
            }
        }

        LocalDate dueDate;
        if (billingCycleType == BillingCycleType.CONSOLIDATED && group != null) {
            dueDate = group.getNextDueDate();
        } else if (loanStructure == LoanStructure.LUMP_SUM) {
            dueDate = LoanScheduleCalculator.maturityDate(disbursementDate, terms);
        } else {
            dueDate = null;
        }

        Loan toSave = Loan.builder()
                .customerId(customerId)
                .productId(productId)
                .originatedProductTerms(terms)
                .principalAmount(principalAmount)
                .outstandingBalance(principalAmount)
                .disbursementDate(disbursementDate)
                .dueDate(dueDate)
                .loanStructure(loanStructure)
                .billingCycleType(billingCycleType)
                .state(LoanState.OPEN)
                .consolidatedBillingGroup(group)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Loan loan = loanRepository.save(toSave);

        if (loanStructure == LoanStructure.INSTALLMENT) {
            installmentService.createScheduleIfNeeded(loan);
            installmentRepository.findByLoanIdOrderByInstallmentNumber(loan.getId()).stream()
                    .min(java.util.Comparator.comparing(LoanInstallment::getDueDate))
                    .ifPresent(first -> loan.setDueDate(first.getDueDate()));
        }

        feeService.applyServiceFeeAtOrigination(loan, disbursementDate);
        reconcileInstallmentOutstanding(loan);

        historyService.record(loan, LoanState.OPEN, null);
        notificationPublisher.publish(NotificationEventType.LOAN_CREATED, customerId, loan);
        notificationPublisher.publish(NotificationEventType.DISBURSEMENT, customerId, loan);
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan cancel(Long loanId, String reason) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
        if (loan.getState() != LoanState.OPEN && loan.getState() != LoanState.OVERDUE) {
            throw new IllegalStateException("Loan cannot be cancelled in state " + loan.getState());
        }
        loan.setState(LoanState.CANCELLED);
        loan.setCancellationReason(reason);
        loan.setUpdatedAt(Instant.now());
        historyService.record(loan, LoanState.CANCELLED, null);
        return loanRepository.save(loan);
    }

    @Transactional(readOnly = true)
    public Optional<Loan> find(Long id) {
        return loanRepository.findById(id);
    }

    public void reconcileInstallmentOutstanding(Loan loan) {
        if (loan.getLoanStructure() != LoanStructure.INSTALLMENT) {
            return;
        }
        BigDecimal sum = installmentRepository.findByLoanIdOrderByInstallmentNumber(loan.getId()).stream()
                .map(LoanInstallment::getOutstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        loan.setOutstandingBalance(sum);
    }
}
