package com.ezra.loanbackend.repository;

import com.ezra.loanbackend.domain.Loan;
import com.ezra.loanbackend.domain.LoanInstallment;
import com.ezra.loanbackend.constants.LoanState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long> {
    List<LoanInstallment> findByLoanIdOrderByInstallmentNumber(Long loanId);

    List<LoanInstallment> findByLoanIdAndDueDateBeforeAndOutstandingAmountGreaterThan(
            Long loanId, LocalDate date, BigDecimal zero);

    @Query("""
            select distinct i.loan from LoanInstallment i
            where i.loan.state = :open
            and i.dueDate < :asOf
            and i.outstandingAmount > 0
            """)
    List<Loan> findLoansWithOverdueInstallments(@Param("open") LoanState open, @Param("asOf") LocalDate asOf);
}
