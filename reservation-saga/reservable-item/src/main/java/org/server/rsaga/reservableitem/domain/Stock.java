package org.server.rsaga.reservableitem.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Value;
import org.server.rsaga.reservableitem.domain.constant.Unit;

@Value
@Embeddable
public class Stock {
    Long quantity;

    @Enumerated(EnumType.STRING)
    Unit unit;

    public Stock(Long quantity, Unit unit) {
        checkQuantity(quantity);
        this.quantity = quantity;

        checkUnit(unit);
        this.unit = unit;
    }

    /**
     * ---------------------- setter ----------------------
     */
    public Stock increaseQuantity(Long increment) {
        if (increment == null || increment < 0) {
            throw new IllegalArgumentException("Increment must not be null or negative.");
        }

        long newQuantity = this.quantity + increment;
        return new Stock(newQuantity, this.unit);
    }

    public Stock decreaseQuantity(Long decrement) {
        if (decrement == null || decrement < 0) {
            throw new IllegalArgumentException("Decrement must not be null or negative.");
        }

        long newQuantity = this.quantity - decrement;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Stock quantities must not be negative.");
        }
        return new Stock(newQuantity, this.unit);
    }

    /**
     * ---------------------- validation ----------------------
     */
    private void checkQuantity(Long quantity) {
        if (quantity == null || quantity < 0L) {
            throw new IllegalArgumentException("Stock quantities must not be a negative value.");
        }
    }

    private void checkUnit(Unit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("Stock units must not be null.");
        }
    }

    protected Stock() {
        this.quantity = 0L;
        this.unit = null;
    }
}