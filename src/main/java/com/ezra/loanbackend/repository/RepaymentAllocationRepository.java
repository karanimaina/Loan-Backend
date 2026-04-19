package com.ezra.loanbackend.repository;

import com.ezra.loanbackend.domain.RepaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepaymentAllocationRepository extends JpaRepository<RepaymentAllocation, Long> {
}
