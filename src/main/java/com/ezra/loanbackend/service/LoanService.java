package com.ezra.loanbackend.service;

import com.ezra.loanbackend.constants.*;
import com.ezra.loanbackend.domain.ConsolidatedBillingGroup;
import com.ezra.loanbackend.domain.Loan;
import com.ezra.loanbackend.domain.LoanInstallment;
import com.ezra.loanbackend.domain.OriginatedProduct;
import com.ezra.loanbackend.integration.customer.Customer;
import com.ezra.loanbackend.integration.customer.CustomerService;
import com.ezra.loanbackend.integration.product.Product;
import com.ezra.loanbackend.integration.product.ProductService;
import com.ezra.loanbackend.exceptions.LoanException;
import com.ezra.loanbackend.integration.product.ProductTermsMapper;
import com.ezra.loanbackend.notification.LoanNotificationPublisher;
import com.ezra.loanbackend.repository.ConsolidatedBillingGroupRepository;
import com.ezra.loanbackend.repository.LoanInstallmentRepository;
import com.ezra.loanbackend.repository.LoanRepository;
import com.ezra.loanbackend.service.schedule.LoanScheduleCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanRepository loanRepository;
    private final ConsolidatedBillingGroupRepository billingGroupRepository;
    private final LoanInstallmentRepository installmentRepository;
    private final InstallmentService installmentService;
    private final FeeService feeService;
    private final LoanStateHistoryService historyService;
    private final LoanNotificationPublisher notificationPublisher;
    private final CustomerService customerService;
    private final ProductService productService;

    @Transactional
    public Loan createCustomerLoan(
            Long customerId,
            Long productId,
            BigDecimal principalAmount,
            LoanStructure loanStructure,
            BillingCycleType billingCycleType,
            LocalDate disbursementDate,
            Long consolidatedBillingGroupId,
            LocalDate newGroupNextDueDate,
            String consolidatedGroupLabel) {
        Customer customer = customerService.getCustomer(customerId);
        Product product = productService.getProduct(productId);
        if (!product.active()) {
            throw LoanException.conflict("Product is inactive");
        }

       BigDecimal current = loanRepository.sumOutstandingForCustomer(customerId,
                List.of(LoanState.OPEN, LoanState.OVERDUE));


        if (current.add(principalAmount).compareTo(customer.loanLimitAmount()) > 0) {
            throw LoanException.conflict("Principal exceeds customer loan limit");
        }

        OriginatedProduct terms = ProductTermsMapper.fromRemote(product);

        ConsolidatedBillingGroup group = null;
        if (billingCycleType == BillingCycleType.CONSOLIDATED) {
            if (consolidatedBillingGroupId != null) {
                group = billingGroupRepository.findById(consolidatedBillingGroupId)
                        .orElseThrow(() -> LoanException.notFound("Billing group not found"));
                if (!group.getCustomerId().equals(customerId)) {
                    throw LoanException.conflict("Billing group does not belong to this customer");
                }
            } else if (newGroupNextDueDate != null) {
                group = billingGroupRepository.save(ConsolidatedBillingGroup.builder()
                        .customerId(customerId)
                        .label(consolidatedGroupLabel != null ? consolidatedGroupLabel : "Consolidated")
                        .nextDueDate(newGroupNextDueDate)
                        .createdAt(Instant.now())
                        .build());
            } else {
                throw LoanException.badRequest("Consolidated billing requires a group id or newConsolidatedGroupNextDueDate");
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
                .originatedProduct(terms)
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
        try {
            notificationPublisher.publish(NotificationEventType.LOAN_CREATED, customerId, loan);
            notificationPublisher.publish(NotificationEventType.DISBURSEMENT, customerId, loan);
        } catch (Exception ex) {
            // Loan creation remains successful even if async notification publishing is unavailable.
            log.warn("Loan {} created but notification publish failed: {}", loan.getId(), ex.getMessage());
        }
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan cancel(Long loanId, String reason) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> LoanException.notFound("Loan not found"));
        if (loan.getState() != LoanState.OPEN && loan.getState() != LoanState.OVERDUE) {
            throw LoanException.conflict("Loan cannot be cancelled in state " + loan.getState());
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
