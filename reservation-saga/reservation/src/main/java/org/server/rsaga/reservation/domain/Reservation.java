package org.server.rsaga.reservation.domain;

import io.hypersistence.tsid.TSID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.server.rsaga.common.domain.BaseTime;
import org.server.rsaga.reservation.domain.constant.ReservationStatus;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor
public class Reservation {

    /**
     * TSID 사용
      */    
    @Id
    @Column(name = "reservation_id", nullable = false)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reservable_item_Id", nullable = false)
    private Long reservableItemId;

    @Column(name = "reservable_time_Id", nullable = false)
    private Long reservableTimeId;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    @Embedded
    private BaseTime baseTime;
    public Reservation(Long id, Long businessId, Long userId, Long reservableItemId, Long reservableTimeId, Long quantity) {
        checkNull(id);
        checkTsid(id);
        this.id = id;

        checkNull(businessId);
        this.businessId = businessId;

        checkNull(userId);
        this.userId = userId;

        checkNull(reservableItemId);
        this.reservableItemId = reservableItemId;

        checkNull(reservableTimeId);
        this.reservableTimeId = reservableTimeId;

        checkQuantity(quantity);
        this.quantity = quantity;

        this.reservationStatus = ReservationStatus.PENDING;
    }

    public long getId() {
        return this.id;
    }

    /**
     * ---------------------- setter ----------------------
     */

    public void updateStatus(ReservationStatus reservationStatus) {
        if (ReservationStatus.PENDING.equals(this.reservationStatus) || ReservationStatus.RESERVED.equals(this.reservationStatus)) {
            this.reservationStatus = reservationStatus;
        }
        else {
            throw new IllegalArgumentException("Changes can only be made when an reservation is in the 'RESERVED' or 'PENDING' state.");
        }
    }


    /**
     * ---------------------- validation ----------------------
     */
    private void checkTsid(long id) {
        try {
            TSID tsid = TSID.from(id);
            long creationTimeMillis = tsid.getUnixMilliseconds();

            long currentTimeMillis = System.currentTimeMillis();
            long minutesInMillis = 60L * 1000L;

            if (currentTimeMillis - creationTimeMillis > minutesInMillis) {
                throw new IllegalArgumentException("The TSID was not created within the last 1 minutes.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("The id must be in the format TSID.");
        }
    }

    private void checkNull(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("The value must not be null.");
        }
    }

    private void checkQuantity(Long quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("The quantity must not be null.");
        }

        if (quantity < 1) {
            throw new IllegalArgumentException("The quantity must be a positive value.");
        }
    }
}