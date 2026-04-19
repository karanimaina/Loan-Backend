package com.ezra.loanbackend.integration.stub;

import com.ezra.loanbackend.integration.customer.CustomerServiceGateway;
import com.ezra.loanbackend.integration.customer.RemoteCustomerDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(name = "loan.integration.stub", havingValue = "true")
public class StubCustomerServiceGateway implements CustomerServiceGateway {

    @Override
    public RemoteCustomerDto getCustomer(Long customerId) {
        return new RemoteCustomerDto(customerId, new BigDecimal("999999.00"));
    }
}
