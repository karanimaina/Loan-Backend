package com.ezra.loanbackend.config;

import com.ezra.loanbackend.integration.LoanIntegrationProperties;
import com.ezra.loanbackend.notification.LoanNotificationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({LoanIntegrationProperties.class, LoanNotificationProperties.class})
public class IntegrationClientConfiguration {

    @Bean
    @Qualifier("customerRestClient")
    @ConditionalOnProperty(name = "loan.integration.stub", havingValue = "false", matchIfMissing = true)
    public RestClient customerRestClient(LoanIntegrationProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getCustomerServiceBaseUrl())
                .build();
    }

    @Bean
    @Qualifier("productRestClient")
    @ConditionalOnProperty(name = "loan.integration.stub", havingValue = "false", matchIfMissing = true)
    public RestClient productRestClient(LoanIntegrationProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getProductServiceBaseUrl())
                .build();
    }
}
