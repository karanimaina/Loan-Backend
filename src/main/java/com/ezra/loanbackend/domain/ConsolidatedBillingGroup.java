package com.ezra.loanbackend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "consolidated_billing_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsolidatedBillingGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** External Customer Service identifier. */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(length = 120)
    private String label;

    /** Shared due date for all loans in this group. */
    @Column(nullable = false)
    private LocalDate nextDueDate;

    @Column(nullable = false)
    private java.time.Instant createdAt;
}
