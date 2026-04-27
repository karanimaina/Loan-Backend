package com.ezra.loanbackend.dto;

import com.ezra.loanbackend.constants.LoanState;

import java.math.BigDecimal;

public record CustomerExposureResponse(Long customerId, BigDecimal totalOutstanding, long activeLoanCount) {
}

