package com.ezra.loanbackend.web;

import com.ezra.loanbackend.domain.Loan;
import com.ezra.loanbackend.service.LoanService;
import com.ezra.loanbackend.web.dto.LoanDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanDtos.LoanResponse originate(@Valid @RequestBody LoanDtos.OriginateLoanRequest req) {
        Loan loan = loanService.originate(
                req.customerId(),
                req.productId(),
                req.principalAmount(),
                req.loanStructure(),
                req.billingCycleType(),
                req.disbursementDate(),
                req.consolidatedBillingGroupId(),
                req.newConsolidatedGroupNextDueDate(),
                req.consolidatedGroupLabel());
        return toResponse(loan);
    }

    @GetMapping("/{id}")
    public LoanDtos.LoanResponse get(@PathVariable Long id) {
        return toResponse(loanService.find(id).orElseThrow(() -> new IllegalArgumentException("Loan not found: " + id)));
    }

    @PostMapping("/{id}/cancel")
    public LoanDtos.LoanResponse cancel(@PathVariable Long id, @RequestBody(required = false) LoanDtos.CancelLoanRequest req) {
        String reason = req != null ? req.reason() : null;
        return toResponse(loanService.cancel(id, reason));
    }

    private LoanDtos.LoanResponse toResponse(Loan loan) {
        Long groupId = loan.getConsolidatedBillingGroup() != null ? loan.getConsolidatedBillingGroup().getId() : null;
        return new LoanDtos.LoanResponse(
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
