package com.ezra.loanbackend.notification;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "loan.notifications")
public class LoanNotificationProperties {

    /** Disable outbound notifications (e.g. tests without Kafka). */
    private boolean enabled = true;

    /** Kafka topic consumed by the Notification service. */
    private String topic = "loan.notifications";
}
