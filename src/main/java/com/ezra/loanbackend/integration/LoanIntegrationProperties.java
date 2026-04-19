package com.ezra.loanbackend.integration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Remote Customer and Product service HTTP settings. When {@code stub=true}, in-process stubs are used (tests/local only).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "loan.integration")
public class LoanIntegrationProperties {

    /** When true, do not call remote services (use stub gateways in {@code integration.stub}). */
    private boolean stub;

    private String customerServiceBaseUrl = "http://localhost:8081";

    /** URI template with {@code {id}} placeholder, e.g. {@code /api/v1/customers/{id}} */
    private String customerByIdPath = "/api/v1/customers/{id}";

    private String productServiceBaseUrl = "http://localhost:8082";

    /** URI template with {@code {id}} placeholder, e.g. {@code /api/v1/products/{id}} */
    private String productByIdPath = "/api/v1/products/{id}";
}
