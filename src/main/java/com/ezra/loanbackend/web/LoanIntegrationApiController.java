package com.ezra.loanbackend.web;

import com.ezra.loanbackend.domain.LoanState;
import com.ezra.loanbackend.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * Endpoints intended for other domain services (Customer, Product) to query loan exposure and usage.
 */
@RestController
@RequestMapping("/api/integration/v1")
@RequiredArgsConstructor
public class LoanIntegrationApiController {

    private final LoanRepository loanRepository;

    @GetMapping("/customers/{customerId}/exposure")
    public CustomerExposureResponse getCustomerExposure(@PathVariable Long customerId) {
        BigDecimal outstanding = loanRepository.sumOutstandingForCustomer(
                customerId,
                List.of(LoanState.OPEN, LoanState.OVERDUE));
        long activeLoans = loanRepository.findByCustomerId(customerId).stream()
                .filter(l -> l.getState() == LoanState.OPEN || l.getState() == LoanState.OVERDUE)
                .count();
        return new CustomerExposureResponse(customerId, outstanding, activeLoans);
    }

    @GetMapping("/customers/{customerId}/loans")
    public List<LoanSummaryResponse> listLoansForCustomer(@PathVariable Long customerId) {
        return loanRepository.findByCustomerId(customerId).stream()
                .map(l -> new LoanSummaryResponse(
                        l.getId(),
                        l.getProductId(),
                        l.getState(),
                        l.getOutstandingBalance(),
                        l.getDueDate()))
                .toList();
    }

    @GetMapping("/products/{productId}/active-loans/count")
    public ProductLoanCountResponse countActiveLoansForProduct(@PathVariable Long productId) {
        long n = loanRepository.countByProductIdAndStateIn(
                productId,
                List.of(LoanState.OPEN, LoanState.OVERDUE));
        return new ProductLoanCountResponse(productId, n);
    }

    public record CustomerExposureResponse(Long customerId, BigDecimal totalOutstanding, long activeLoanCount) {
    }

    public record LoanSummaryResponse(
            Long loanId,
            Long productId,
            LoanState state,
            BigDecimal outstandingBalance,
            java.time.LocalDate dueDate
    ) {
    }

    public record ProductLoanCountResponse(Long productId, long activeLoanCount) {
    }
}
