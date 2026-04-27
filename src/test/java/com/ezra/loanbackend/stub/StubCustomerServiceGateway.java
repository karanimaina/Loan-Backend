package com.ezra.loanbackend.stub;

import com.ezra.loanbackend.integration.customer.Customer;
import com.ezra.loanbackend.integration.customer.CustomerService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(name = "loan.integration.stub", havingValue = "true")
public class StubCustomerServiceGateway implements CustomerService {

    @Override
    public Customer getCustomer(Long customerId) {
        return new Customer(customerId, new BigDecimal("999999.00"));
    }
}
