package com.ezra.loanbackend.notification;

import java.util.Map;
public record LoanNotificationMessage(
        String eventType,
        Long customerId,
        Long loanId,
        Long productId,
        Map<String, Object> payload,
        String occurredAt
) {
}
