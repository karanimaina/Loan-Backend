package com.ezra.loanbackend.controller;

import com.ezra.loanbackend.domain.LoanRepayment;
import com.ezra.loanbackend.dto.RepaymentDtos;
import com.ezra.loanbackend.dto.UniversalResponse;
import com.ezra.loanbackend.service.RepaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans/{loanId}/repayments")
@RequiredArgsConstructor
public class RepaymentController {

    private final RepaymentService repaymentService;

    @PostMapping
    public ResponseEntity<UniversalResponse> record(
            @PathVariable Long loanId,
            @Valid @RequestBody RepaymentDtos.RecordRepaymentRequest req) {
        LoanRepayment r = repaymentService.recordRepayment(
                loanId,
                req.amount(),
                req.paymentDate(),
                req.channel(),
                req.externalReference());
        RepaymentDtos.RepaymentResponse body = new RepaymentDtos.RepaymentResponse(
                r.getId(),
                r.getLoan().getId(),
                r.getAmount(),
                r.getPaymentDate(),
                r.getChannel(),
                r.getExternalReference());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UniversalResponse.success(HttpStatus.CREATED, "Repayment recorded", body));
    }
}
