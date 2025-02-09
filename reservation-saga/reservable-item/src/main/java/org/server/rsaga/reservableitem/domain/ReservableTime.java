package org.server.rsaga.reservableitem.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.server.rsaga.common.domain.BaseTime;

import java.util.Date;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservableTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservable_time_id", nullable = false)
    private Long id;

    @Embedded
    private Stock stock;

    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservable_item_id", nullable = false)
    private ReservableItem reservableItem;

    private boolean isTimeAvailable;

    @Embedded
    private BaseTime baseTime;

    public ReservableTime(
            final Date time,
            final Stock stock,
            final boolean isTimeAvailable) {
        checkReservableTime(time);
        this.time = time;

        checkStock(stock);
        this.stock = stock;

        this.isTimeAvailable = isTimeAvailable;
    }

    /**
     * ---------------------- setter ----------------------
     */
    public void adjustStock(Stock newStock) {
        if (newStock != null) {
            this.stock = newStock;
        }
    }

    public void rescheduleDate(Date newReservableTime) {
        if (newReservableTime != null) {
            checkIfDateIsInFuture(newReservableTime);
            this.time = newReservableTime;
        }
    }

    public void assignReservableItem(ReservableItem reservableItem) {
        this.reservableItem = reservableItem;
    }

    public void changeIsTimeAvailable(Boolean newIsTimeAvailable) {
        if (newIsTimeAvailable != null) {
            this.isTimeAvailable = newIsTimeAvailable;
        }
    }

    public void increaseStock(Long quantity) {
        this.stock = this.stock.increaseQuantity(quantity);
    }

    public void decreaseStock(Long quantity) {
        this.stock = this.stock.decreaseQuantity(quantity);
    }

    /**
     * ---------------------- validation ----------------------
     */
    private void checkReservableTime(Date reservableTime) {
        if (reservableTime == null) {
            throw new IllegalArgumentException("The reservableTime must not be null.");
        }
        checkIfDateIsInFuture(reservableTime);
    }

    private void checkIfDateIsInFuture(Date date) {
        Date now = new Date();
        if (date.before(now)) {
            throw new IllegalArgumentException("The date must be after today.");
        }
    }

    private void checkStock(Stock stock) {
        if (stock == null) {
            throw new IllegalArgumentException("The stock must not be null.");
        }
    }
}