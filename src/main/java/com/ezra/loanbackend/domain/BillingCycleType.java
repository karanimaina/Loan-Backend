package com.ezra.loanbackend.domain;

public enum BillingCycleType {
    /** Due dates derived from loan origination and tenure/installments. */
    INDIVIDUAL,
    /** Multiple loans share one consolidated due date (see ConsolidatedBillingGroup). */
    CONSOLIDATED
}
