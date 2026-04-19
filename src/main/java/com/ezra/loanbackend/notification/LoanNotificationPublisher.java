package com.ezra.loanbackend.notification;

import com.ezra.loanbackend.domain.Loan;
import com.ezra.loanbackend.domain.NotificationEventType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Publishes loan lifecycle events to Kafka for the Notification service.
 * <p>
 * For Spring Cloud Stream, wrap this topic with a binder or replace this bean with a {@code StreamBridge}-based
 * publisher (see README) so messages flow through your Cloud Stream configuration.
 */
@Service
public class LoanNotificationPublisher {

    private final LoanNotificationProperties properties;
    private final JsonMapper jsonMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public LoanNotificationPublisher(
            LoanNotificationProperties properties,
            JsonMapper jsonMapper,
            KafkaTemplate<String, String> kafkaTemplate) {
        this.properties = properties;
        this.jsonMapper = jsonMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(NotificationEventType eventType, Long customerId, Loan loan) {
        if (!properties.isEnabled()) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        if (loan != null) {
            payload.put("outstandingBalance", loan.getOutstandingBalance());
            if (loan.getDueDate() != null) {
                payload.put("dueDate", loan.getDueDate().toString());
            }
            payload.put("state", loan.getState().name());
        }
        LoanNotificationMessage message = new LoanNotificationMessage(
                eventType.name(),
                customerId,
                loan != null ? loan.getId() : null,
                loan != null ? loan.getProductId() : null,
                payload,
                Instant.now());
        try {
            String json = jsonMapper.writeValueAsString(message);
            kafkaTemplate.send(properties.getTopic(), String.valueOf(customerId), json);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize notification", e);
        }
    }
}
