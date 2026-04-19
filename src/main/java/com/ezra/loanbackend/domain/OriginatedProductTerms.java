package com.ezra.loanbackend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Snapshot of product terms and fees at loan origination (sourced from Product Service).
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OriginatedProductTerms {

    @Enumerated(EnumType.STRING)
    @Column(name = "product_tenure_type", length = 16)
    private TenureType tenureType;

    @Column(name = "product_tenure_value")
    private Integer tenureValue;

    @Column(name = "product_fixed_term", nullable = false)
    private boolean fixedTerm;

    @Column(name = "product_number_of_installments")
    private Integer numberOfInstallments;

    @Embedded
    private ProductFeeConfiguration feeConfiguration;
}
