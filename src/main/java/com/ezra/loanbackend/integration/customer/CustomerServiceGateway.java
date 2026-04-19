package com.ezra.loanbackend.integration.customer;

public interface CustomerServiceGateway {

    RemoteCustomerDto getCustomer(Long customerId);
}
