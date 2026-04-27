package com.ezra.loanbackend.repository;

import com.ezra.loanbackend.constants.FeeType;
import com.ezra.loanbackend.domain.LoanFeeCharge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanFeeChargeRepository extends JpaRepository<LoanFeeCharge, Long> {

    boolean existsByLoanIdAndFeeTypeAndAppliedDate(Long loanId, FeeType feeType, java.time.LocalDate appliedDate);
}
