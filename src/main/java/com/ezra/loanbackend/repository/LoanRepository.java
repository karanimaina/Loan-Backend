package com.ezra.loanbackend.repository;

import com.ezra.loanbackend.domain.Loan;
import com.ezra.loanbackend.constants.LoanState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByCustomerId(Long customerId);

    @Query("""
            select l from Loan l
            where l.state in :states
            and l.dueDate is not null
            and l.dueDate < :asOf
            """)
    List<Loan> findDueBefore(@Param("states") Collection<LoanState> states, @Param("asOf") LocalDate asOf);

    @Query("""
            select l from Loan l
            where l.state = :state
            and l.dueDate is not null
            and l.dueDate < :cutoff
            """)
    List<Loan> findByStateAndDueDateBefore(@Param("state") LoanState state, @Param("cutoff") LocalDate cutoff);

    @Query("""
            select coalesce(sum(l.outstandingBalance), 0)
            from Loan l
            where l.customerId = :customerId
            and l.state in :states
            """)
    BigDecimal sumOutstandingForCustomer(@Param("customerId") Long customerId, @Param("states") Collection<LoanState> states);

    long countByProductIdAndStateIn(Long productId, Collection<LoanState> states);
}
