package com.ezra.loanbackend.notification;

import com.ezra.loanbackend.domain.Loan;
import com.ezra.loanbackend.constants.NotificationEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanNotificationPublisher {
    private final StreamBridge streamBridge;

    @Async("notificationExecutor")
    public void publish(NotificationEventType eventType, Long customerId, Loan loan) {
        try {
            LoanNotificationMessage.Notification notification = new LoanNotificationMessage.Notification(
                    eventType.name(),
                    loan != null ? loan.getId() : null,
                    loan != null ? loan.getProductId() : null,
                    loan != null ? loan.getState().name() : null,
                    loan != null ? loan.getOutstandingBalance() : null,
                    loan != null && loan.getDueDate() != null ? loan.getDueDate().toString() : null
            );
            LoanNotificationMessage message = new LoanNotificationMessage(
                    "loan-backend",
                    UUID.randomUUID().toString(),
                    Instant.now().toString(),
                    notification,
                    new LoanNotificationMessage.Customer(customerId));

            String loanNotificationBinder = "loan-notifications-out-0";
            streamBridge.send(loanNotificationBinder, message);
        } catch (Exception ex) {
            log.warn("Notification dispatch failed for event {} and loan {}: {}",
                    eventType,
                    loan != null ? loan.getId() : null,
                    ex.getMessage());
        }
    }
}
