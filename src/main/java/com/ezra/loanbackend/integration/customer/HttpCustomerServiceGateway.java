package com.ezra.loanbackend.integration.customer;

import com.ezra.loanbackend.integration.LoanIntegrationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@ConditionalOnProperty(name = "loan.integration.stub", havingValue = "false", matchIfMissing = true)
public class HttpCustomerServiceGateway implements CustomerServiceGateway {

    private final RestClient customerRestClient;

    private final LoanIntegrationProperties properties;

    public HttpCustomerServiceGateway(
            @Qualifier("customerRestClient") RestClient customerRestClient,
            LoanIntegrationProperties properties) {
        this.customerRestClient = customerRestClient;
        this.properties = properties;
    }

    @Override
    public RemoteCustomerDto getCustomer(Long customerId) {
        try {
            return customerRestClient.get()
                    .uri(properties.getCustomerByIdPath(), customerId)
                    .retrieve()
                    .body(RemoteCustomerDto.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new IllegalArgumentException("Customer not found in Customer Service: " + customerId);
            }
            throw ex;
        }
    }
}
