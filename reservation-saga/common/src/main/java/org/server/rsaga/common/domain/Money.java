package org.server.rsaga.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Value;

@Value
@Embeddable
public class Money {
    @Column(nullable = false)
    Long amount;

    public Money(final Long amount) {
        if (amount == null || amount < 0) {
            throw new IllegalArgumentException("The money amount should be a negative value.");
        }
        this.amount = amount;
    }

    public Money add(final Money other) {
        return new Money(this.amount + other.amount);
    }

    public Money subtract(final Money other) {
        long calculatedAmount = this.amount - other.amount;
        if (calculatedAmount < 0) {
            throw new IllegalArgumentException("The calculated money amount should not be a negative value.");
        }
        return new Money(calculatedAmount);
    }

    protected Money(){
        this.amount = 0L;
    }
}