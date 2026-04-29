package com.ezra.loanbackend.integration.product;

import com.ezra.loanbackend.domain.OriginatedProduct;

public final class ProductTermsMapper {

    private ProductTermsMapper() {
    }

    public static OriginatedProduct fromRemote(Product product) {
        return OriginatedProduct.builder()
                .tenureType(product.tenureType())
                .tenureValue(product.tenureValue())
                .fixedTerm(product.fixedTerm())
                .numberOfInstallments(product.numberOfInstallments())
                .feeConfiguration(product.feeConfiguration())
                .build();
    }
}
