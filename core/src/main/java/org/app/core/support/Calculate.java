package org.app.core.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Calculate {

    // use string for sure
    public static BigDecimal money(String money) {
        return new BigDecimal(money);
    }

    public static boolean isEqual(BigDecimal money1, BigDecimal money2) {
        return money1.compareTo(money2) == 0;
    }

    public static boolean isLessThan(BigDecimal money1, BigDecimal money2) {
        return money1.compareTo(money2) < 0;
    }

    public static boolean isGreaterThan(BigDecimal money1, BigDecimal money2) {
        return money1.compareTo(money2) > 0;
    }

    public static BigDecimal add(BigDecimal money1, BigDecimal money2) {
        return money1.add(money2);
    }

    public static BigDecimal subtract(BigDecimal money1, BigDecimal money2) {
        return money1.subtract(money2);
    }

    public static BigDecimal multiply(BigDecimal money1, BigDecimal money2) {
        return money1.multiply(money2);
    }

    public static BigDecimal divide(BigDecimal money1, BigDecimal money2) {
        return money1.divide(money2, 2, RoundingMode.HALF_UP);
    }
}
