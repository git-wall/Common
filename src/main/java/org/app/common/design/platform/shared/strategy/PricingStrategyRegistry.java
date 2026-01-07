package org.app.common.design.platform.shared.strategy;

import org.app.common.design.platform.domain.model.customer.CustomerTier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;

@Component
public class PricingStrategyRegistry {

    private final Map<CustomerTier, Function<BigDecimal, BigDecimal>> strategies;
    private static final BigDecimal THREE_PERCENT = new BigDecimal("0.97");
    private static final BigDecimal FIVE_PERCENT = new BigDecimal("0.95");     // 3% off
    private static final BigDecimal TEN_PERCENT = new BigDecimal("0.90");      // 5% off
    private static final BigDecimal FIFTEEN_PERCENT = new BigDecimal("0.85");  // 10% off
    private static final BigDecimal ONE_THOUSAND = new BigDecimal("1000");

    public PricingStrategyRegistry() {
        this.strategies = Map.of(
            CustomerTier.REGULAR, price -> price,
            CustomerTier.SILVER, price -> price.multiply(THREE_PERCENT),
            CustomerTier.GOLD, price -> price.multiply(FIVE_PERCENT),
            CustomerTier.PLATINUM, price -> price.multiply(TEN_PERCENT),
            CustomerTier.VIP, this::calculateVipPrice
        );
    }

    public Function<BigDecimal, BigDecimal> getStrategy(CustomerTier tier) {
        return strategies.getOrDefault(tier, Function.identity());
    }

    private BigDecimal calculateVipPrice(BigDecimal price) {
        BigDecimal baseDiscount = FIFTEEN_PERCENT; // 15% off

        // Extra 5% for orders > $1000
        if (price.compareTo(ONE_THOUSAND) > 0) {
            baseDiscount = baseDiscount.multiply(new BigDecimal("0.95"));
        }

        return price.multiply(baseDiscount);
    }
}
