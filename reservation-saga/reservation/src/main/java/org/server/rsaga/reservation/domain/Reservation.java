package org.server.rsaga.reservation.domain;

import io.hypersistence.tsid.TSID;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.server.rsaga.common.domain.BaseTime;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.reservation.domain.constant.ReservationStatus;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    /**
     * TSID 사용
      */    
    @Id
    @Column(name = "reservation_id", nullable = false)
    private Long id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "business_id", nullable = false))
    private ForeignKey businessId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private ForeignKey userId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "reservable_item_id", nullable = false))
    private ForeignKey reservableItemId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "reservable_time_id", nullable = false))
    private ForeignKey reservableTimeId;

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

        this.businessId = new ForeignKey(businessId);

        this.userId = new ForeignKey(userId);

        this.reservableItemId = new ForeignKey(reservableItemId);

        this.reservableTimeId = new ForeignKey(reservableTimeId);

        checkQuantity(quantity);
        this.quantity = quantity;

        this.reservationStatus = ReservationStatus.PENDING;
    }

    /**
     * ---------------------- getter ----------------------
     */

    public long getId() {
        return this.id;
    }

    public long getBusinessId() {
        return businessId.getId();
    }

    public long getUserId() {
        return userId.getId();
    }

    public long getReservableItemId() {
        return reservableItemId.getId();
    }

    public long getReservableTimeId() {
        return reservableTimeId.getId();
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