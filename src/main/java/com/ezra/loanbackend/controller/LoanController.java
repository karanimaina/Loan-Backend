package com.ezra.loanbackend.controller;

import com.ezra.loanbackend.constants.BillingCycleType;
import com.ezra.loanbackend.constants.LoanStructure;
import com.ezra.loanbackend.domain.Loan;
import com.ezra.loanbackend.dto.CancelLoanRequest;
import com.ezra.loanbackend.dto.LoanRequest;
import com.ezra.loanbackend.dto.LoanResponse;
import com.ezra.loanbackend.dto.UniversalResponse;
import com.ezra.loanbackend.exceptions.LoanException;
import com.ezra.loanbackend.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Slf4j
public class LoanController {
    private final LoanService loanService;
    @PostMapping("/create")
    public ResponseEntity<UniversalResponse> originate(@Valid @RequestBody LoanRequest req) {
        Loan loan = loanService.createCustomerLoan(
                req.customerId(),
                req.productId(),
                req.principalAmount(),
                LoanStructure.valueOf(req.loanStructure()),
                BillingCycleType.valueOf(req.billingCycleType()),
                req.disbursementDate(),
                req.consolidatedBillingGroupId(),
                req.newConsolidatedGroupNextDueDate(),
                req.consolidatedGroupLabel());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UniversalResponse.success(HttpStatus.CREATED, "Loan created", toResponse(loan)));
    }
    @GetMapping("/{id}")
    public ResponseEntity<UniversalResponse> get(@PathVariable Long id) {
        Loan loan = loanService.find(id)
                .orElseThrow(() -> LoanException.notFound("Loan not found: " + id));
        return ResponseEntity.ok(UniversalResponse.success(toResponse(loan)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<UniversalResponse> cancel(@PathVariable Long id, @RequestBody(required = false) CancelLoanRequest req) {
        String reason = req != null ? req.reason() : null;
        Loan loan = loanService.cancel(id, reason);
        return ResponseEntity.ok(UniversalResponse.success("Loan cancelled", toResponse(loan)));
    }

    private LoanResponse toResponse(Loan loan) {
        Long groupId = loan.getConsolidatedBillingGroup() != null ? loan.getConsolidatedBillingGroup().getId() : null;
        return new LoanResponse(
                loan.getId(),
                loan.getCustomerId(),
                loan.getProductId(),
                loan.getPrincipalAmount(),
                loan.getOutstandingBalance(),
                loan.getDisbursementDate(),
                loan.getDueDate(),
                loan.getLoanStructure(),
                loan.getBillingCycleType(),
                loan.getState(),
                loan.getCancellationReason(),
                groupId);
    }
}
