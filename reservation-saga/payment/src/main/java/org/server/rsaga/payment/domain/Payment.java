package org.server.rsaga.payment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.domain.Money;
import org.server.rsaga.payment.domain.constant.PaymentStatus;
import org.server.rsaga.common.domain.constant.PaymentType;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    public Payment(Long userId, Long reservationId, Money amount, PaymentType paymentType, PaymentStatus paymentStatus) {
        this.userId = new ForeignKey(userId);
        this.reservationId = new ForeignKey(reservationId);
        this.amount = amount;
        this.paymentType = paymentType;
        this.paymentStatus = paymentStatus;
    }

    /**
     * ---------------------- getter ----------------------
     */
    public long getAmount() {
        return amount.getAmount();
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