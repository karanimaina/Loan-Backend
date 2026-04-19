package com.ezra.loanbackend.integration.product;

import com.ezra.loanbackend.integration.LoanIntegrationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@ConditionalOnProperty(name = "loan.integration.stub", havingValue = "false", matchIfMissing = true)
public class HttpProductServiceGateway implements ProductServiceGateway {

    private final RestClient productRestClient;
    private final LoanIntegrationProperties properties;

    public HttpProductServiceGateway(
            @Qualifier("productRestClient") RestClient productRestClient,
            LoanIntegrationProperties properties) {
        this.productRestClient = productRestClient;
        this.properties = properties;
    }

    @Override
    public RemoteLoanProductDto getProduct(Long productId) {
        try {
            return productRestClient.get()
                    .uri(properties.getProductByIdPath(), productId)
                    .retrieve()
                    .body(RemoteLoanProductDto.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new IllegalArgumentException("Product not found in Product Service: " + productId);
            }
            throw ex;
        }
    }
}
