package com.ezra.loanbackend.integration.product;

import com.ezra.loanbackend.domain.OriginatedProductTerms;

public final class ProductTermsMapper {

    private ProductTermsMapper() {
    }

    public static OriginatedProductTerms fromRemote(RemoteLoanProductDto product) {
        return OriginatedProductTerms.builder()
                .tenureType(product.tenureType())
                .tenureValue(product.tenureValue())
                .fixedTerm(product.fixedTerm())
                .numberOfInstallments(product.numberOfInstallments())
                .feeConfiguration(product.feeConfiguration())
                .build();
    }
}
