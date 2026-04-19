package com.ezra.loanbackend.integration.stub;

import com.ezra.loanbackend.domain.ProductFeeConfiguration;
import com.ezra.loanbackend.domain.ServiceFeeTiming;
import com.ezra.loanbackend.domain.TenureType;
import com.ezra.loanbackend.integration.product.ProductServiceGateway;
import com.ezra.loanbackend.integration.product.RemoteLoanProductDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(name = "loan.integration.stub", havingValue = "true")
public class StubProductServiceGateway implements ProductServiceGateway {

    @Override
    public RemoteLoanProductDto getProduct(Long productId) {
        return new RemoteLoanProductDto(
                productId,
                "STUB-" + productId,
                "Stub product",
                true,
                TenureType.MONTHS,
                12,
                true,
                12,
                ProductFeeConfiguration.builder()
                        .serviceFeeFixed(new BigDecimal("10.00"))
                        .serviceFeeTiming(ServiceFeeTiming.AT_ORIGINATION)
                        .lateFeeFixed(new BigDecimal("5.00"))
                        .daysAfterDueBeforeLateFee(3)
                        .daysAfterDueForFeeApplication(3)
                        .build());
    }
}
