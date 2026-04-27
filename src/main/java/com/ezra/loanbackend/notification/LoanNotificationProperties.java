package com.ezra.loanbackend.notification;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "loan.notifications")
public class LoanNotificationProperties {

    private boolean enabled = true;
    private String topic = "loan.notifications";
}
