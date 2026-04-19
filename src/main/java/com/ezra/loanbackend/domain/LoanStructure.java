package com.ezra.loanbackend.domain;

public enum LoanStructure {
    /** Single maturity date (lump sum). */
    LUMP_SUM,
    /** Repaid in multiple scheduled installments. */
    INSTALLMENT
}
