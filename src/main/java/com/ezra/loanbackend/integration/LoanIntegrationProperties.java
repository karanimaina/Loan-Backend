package com.ezra.loanbackend.integration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "loan.integration")
public class LoanIntegrationProperties {
    private boolean stub;

    private String customerServiceBaseUrl = "http://localhost:8081";

    private String customerByIdPath = "/api/v1/customers/{id}";

    private String productServiceBaseUrl = "http://localhost:8082";

    private String productByIdPath = "/api/v1/products/{id}";
}
