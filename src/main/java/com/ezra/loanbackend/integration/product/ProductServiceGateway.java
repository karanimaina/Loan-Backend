package com.ezra.loanbackend.integration.product;

public interface ProductServiceGateway {

    RemoteLoanProductDto getProduct(Long productId);
}
