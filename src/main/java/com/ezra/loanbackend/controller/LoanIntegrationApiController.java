package com.ezra.loanbackend.controller;

import com.ezra.loanbackend.constants.LoanState;
import com.ezra.loanbackend.dto.*;
import com.ezra.loanbackend.dto.UniversalResponse;
import com.ezra.loanbackend.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/integration/v1")
@RequiredArgsConstructor
public class LoanIntegrationApiController {

    private final LoanRepository loanRepository;

    @GetMapping("/customers/{customerId}/exposure")
    public ResponseEntity<UniversalResponse> getCustomerExposure(@PathVariable Long customerId) {
        BigDecimal outstanding = loanRepository.sumOutstandingForCustomer(
                customerId,
                List.of(LoanState.OPEN, LoanState.OVERDUE));
        long activeLoans = loanRepository.findByCustomerId(customerId).stream()
                .filter(l -> l.getState() == LoanState.OPEN || l.getState() == LoanState.OVERDUE)
                .count();
        CustomerExposureResponse data = new CustomerExposureResponse(customerId, outstanding, activeLoans);
        return ResponseEntity.ok(UniversalResponse.success(data));
    }

    @GetMapping("/customers/{customerId}/loans")
    public ResponseEntity<UniversalResponse> listLoansForCustomer(@PathVariable Long customerId) {
        List<LoanSummaryResponse> data = loanRepository.findByCustomerId(customerId).stream()
                .map(l -> new LoanSummaryResponse(
                        l.getId(),
                        l.getProductId(),
                        l.getState(),
                        l.getOutstandingBalance(),
                        l.getDueDate()))
                .toList();
        return ResponseEntity.ok(UniversalResponse.success(data));
    }

    @GetMapping("/products/{productId}/active-loans/count")
    public ResponseEntity<UniversalResponse> countActiveLoansForProduct(@PathVariable Long productId) {
        long n = loanRepository.countByProductIdAndStateIn(
                productId,
                List.of(LoanState.OPEN, LoanState.OVERDUE));
        ProductLoanCountResponse data = new ProductLoanCountResponse(productId, n);
        return ResponseEntity.ok(UniversalResponse.success(data));
    }

}
