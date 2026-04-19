package com.ezra.loanbackend.web;

import com.ezra.loanbackend.domain.LoanRepayment;
import com.ezra.loanbackend.service.RepaymentService;
import com.ezra.loanbackend.web.dto.RepaymentDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans/{loanId}/repayments")
@RequiredArgsConstructor
public class RepaymentController {

    private final RepaymentService repaymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RepaymentDtos.RepaymentResponse record(
            @PathVariable Long loanId,
            @Valid @RequestBody RepaymentDtos.RecordRepaymentRequest req) {
        LoanRepayment r = repaymentService.recordRepayment(
                loanId,
                req.amount(),
                req.paymentDate(),
                req.channel(),
                req.externalReference());
        return new RepaymentDtos.RepaymentResponse(
                r.getId(),
                r.getLoan().getId(),
                r.getAmount(),
                r.getPaymentDate(),
                r.getChannel(),
                r.getExternalReference());
    }
}
