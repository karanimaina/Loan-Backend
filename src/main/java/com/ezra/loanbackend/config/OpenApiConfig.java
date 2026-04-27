package com.ezra.loanbackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI loanBackendOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Loan Backend API")
                        .description("Loan management service APIs for origination, repayments, integrations, and sweeps.")
                        .version("v1")
                        .contact(new Contact().name("Loan Backend Team"))
                        .license(new License().name("Internal Use")));
    }
}
