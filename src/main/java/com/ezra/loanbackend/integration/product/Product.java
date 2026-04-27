package com.ezra.loanbackend.integration.product;

import com.ezra.loanbackend.domain.ProductFeeConfiguration;
import com.ezra.loanbackend.constants.TenureType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Product(
        Long id,
        String code,
        String name,
        boolean active,
        TenureType tenureType,
        Integer tenureValue,
        boolean fixedTerm,
        Integer numberOfInstallments,
        ProductFeeConfiguration feeConfiguration
) {
}
