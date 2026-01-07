package org.app.common.design.platform.domain.model.order.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Money {
    private BigDecimal amount;
    private String currency = "USD";

    public Money(BigDecimal amount) {
        this.amount = amount;
    }
}
