package com.ezra.loanbackend.integration.product;

import com.ezra.loanbackend.exceptions.LoanException;
import com.ezra.loanbackend.integration.LoanIntegrationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loan.integration.stub", havingValue = "false", matchIfMissing = true)
public class ProductServiceImpl implements ProductService {

    private final RestClient productRestClient;
    private final LoanIntegrationProperties properties;


    @Override
    public Product getProduct(Long productId) {
        try {
            Product product = productRestClient.get()
                    .uri(properties.getProductByIdPath(), productId)
                    .retrieve()
                    .body(Product.class);
            if (product == null) {
                throw new LoanException(HttpStatus.BAD_GATEWAY, "Invalid product payload received from Product Service");
            }
            return product;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw LoanException.notFound("Product not found in Product Service: " + productId);
            }
            throw new LoanException(
                    HttpStatus.BAD_GATEWAY.value(),
                    "Product Service error: HTTP " + ex.getStatusCode().value(),
                    ex);
        }
    }
}
