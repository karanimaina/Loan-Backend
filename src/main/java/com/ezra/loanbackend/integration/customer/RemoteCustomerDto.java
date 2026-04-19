package com.ezra.loanbackend.integration.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoteCustomerDto(Long id, BigDecimal loanLimitAmount) {
}
