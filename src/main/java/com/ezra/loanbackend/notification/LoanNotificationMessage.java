package com.ezra.loanbackend.notification;

import java.math.BigDecimal;

public record LoanNotificationMessage(
        String source,
        String eventId,
        String eventTime,
        Notification notification,
        Customer customer
) {
    public record Notification(
            String type,
            Long loanId,
            Long productId,
            String state,
            BigDecimal outstandingBalance,
            String dueDate
    ) {
    }

    public record Customer(Long id) {
    }
}
