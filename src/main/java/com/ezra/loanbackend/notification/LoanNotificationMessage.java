package com.ezra.loanbackend.notification;

import java.time.Instant;
import java.util.Map;

/**
 * Payload published to Kafka for the Notification Service (Cloud Stream bridge).
 */
public record LoanNotificationMessage(
        String eventType,
        Long customerId,
        Long loanId,
        Long productId,
        Map<String, Object> payload,
        Instant occurredAt
) {
}
