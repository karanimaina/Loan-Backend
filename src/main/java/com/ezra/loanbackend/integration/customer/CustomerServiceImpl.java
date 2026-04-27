package com.ezra.loanbackend.integration.customer;

import com.ezra.loanbackend.exceptions.LoanException;
import com.ezra.loanbackend.integration.LoanIntegrationProperties;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loan.integration.stub", havingValue = "false", matchIfMissing = true)
public class CustomerServiceImpl implements CustomerService {

    private final RestClient customerRestClient;

    private final LoanIntegrationProperties properties;



    @Override
    public Customer getCustomer(Long customerId) {
        try {
            CustomerEnvelope response = customerRestClient.get()
                    .uri(properties.getCustomerByIdPath(), customerId)
                    .retrieve()
                    .body(CustomerEnvelope.class);

            if (response == null || response.data() == null || response.data().creditProfile() == null) {
                throw new LoanException(HttpStatus.BAD_GATEWAY, "Invalid customer payload received from Customer Service");
            }

            BigDecimal creditLimit = response.data().creditProfile().creditLimit();
            if (creditLimit == null) {
                creditLimit = BigDecimal.ZERO;
            }

            return new Customer(response.data().id(), creditLimit);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw LoanException.notFound("Customer not found in Customer Service: " + customerId);
            }
            throw new LoanException(HttpStatus.BAD_GATEWAY.value(),
                    "Customer Service error: HTTP " + ex.getStatusCode().value(), ex);
        }
    }

    private record CustomerEnvelope(int status, String message, CustomerData data) {
    }

    private record CustomerData(Long id, CreditProfile creditProfile) {
    }

    private record CreditProfile(BigDecimal creditLimit) {
    }
}
