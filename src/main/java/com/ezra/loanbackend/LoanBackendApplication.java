package com.ezra.loanbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LoanBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanBackendApplication.class, args);
    }

}
