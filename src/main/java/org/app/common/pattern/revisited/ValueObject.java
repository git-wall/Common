package org.app.common.pattern.revisited;

import java.util.Objects;
import java.util.function.Function;

public class ValueObject<T> {
    private final T value;
    
    private ValueObject(T value) {
        this.value = Objects.requireNonNull(value);
    }
    
    public static <T> ValueObject<T> of(T value) {
        return new ValueObject<>(value);
    }
    
    public <R> ValueObject<R> map(Function<T, R> mapper) {
        return new ValueObject<>(mapper.apply(value));
    }
    
    public T getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ValueObject)) return false;
        ValueObject<?> that = (ValueObject<?>) o;
        return value.equals(that.value);
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    @Override
    public String toString() {
        return value.toString();
    }

    public static class Money extends ValueObject<java.math.BigDecimal> {
        private final String currency;

        private Money(java.math.BigDecimal amount, String currency) {
            super(amount);
            this.currency = Objects.requireNonNull(currency);
        }

        public static Money of(java.math.BigDecimal amount, String currency) {
            return new Money(amount, currency);
        }

        public Money add(Money other) {
            if (!currency.equals(other.currency)) {
                throw new IllegalArgumentException("Cannot add different currencies");
            }
            return new Money(getValue().add(other.getValue()), currency);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Money)) return false;
            if (!super.equals(o)) return false;
            Money money = (Money) o;
            return currency.equals(money.currency);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), currency);
        }
    }
}