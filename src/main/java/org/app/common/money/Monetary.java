package org.app.common.money;

import java.math.BigDecimal;
import java.util.Currency;

public class Monetary {
    // Eurozone (EUR) – EUR (Euro) (Used by many European countries, including Germany, France, Spain, etc.)
    public static Money EUR(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("EUR"));
    }

    // United Kingdom (GB) – GBP (British Pound Sterling) (English is spoken, but the UK is the country)
    public static Money GBP(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("GBP"));
    }

    // Japan (JP) – JPY (Japanese Yen)
    public static Money JPY(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("JPY"));
    }

    // China (CN) – CNY (Chinese Yuan Renminbi)
    public static Money CNY(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("CNY"));
    }

    // Vietnam (VN) – VND (Vietnamese đồng)
    public static Money VND(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("VND"));
    }

    // United States (US) – USD (United States Dollar)
    public static Money USD(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("USD"));
    }
}
