package org.server.rsaga.payment.domain;

import io.hypersistence.tsid.TSID;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.server.rsaga.common.domain.BaseTime;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.domain.Money;
import org.server.rsaga.payment.domain.constant.PaymentStatus;
import org.server.rsaga.common.domain.constant.PaymentType;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment", indexes = {
        @Index(name = "idx_reservation_id", columnList = "reservation_id")
})
public class Payment {
    /**
     * TSID 사용
     */
    @Id
    @Column(name = "payment_record_id", nullable = false)
    private Long id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private ForeignKey userId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "reservation_id", nullable = false))
    private ForeignKey reservationId;

    @Embedded
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Embedded
    private BaseTime baseTime;

    public Payment(
            final ForeignKey userId,
            final ForeignKey reservationId,
            final Money amount,
            final PaymentType paymentType,
            final PaymentStatus paymentStatus) {
        this.id = TSID.fast().toLong();

        checkNull(userId);
        this.userId = userId;

        checkNull(reservationId);
        this.reservationId = reservationId;

        checkNull(amount);
        this.amount = amount;

        checkNull(paymentType);
        this.paymentType = paymentType;

        checkNull(paymentStatus);
        this.paymentStatus = paymentStatus;
    }

    /**
     * ---------------------- getter ----------------------
     */
    public Money amount() {
        return amount;
    }

    public long getUserId() {
        return userId.getId();
    }

    public long getReservationId() {
        return reservationId.getId();
    }

    /**
     * ---------------------- setter ----------------------
     */
    public void cancel() {
        this.paymentStatus = PaymentStatus.CANCEL;
    }

    /**
     * ---------------------- validation ----------------------
     */
    private void checkNull(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("The value must not be null.");
        }
    }
}