package org.server.rsaga.payment.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.domain.Money;
import org.server.rsaga.common.domain.constant.PaymentType;
import org.server.rsaga.payment.domain.constant.PaymentStatus;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment tests")
class PaymentTest {
    private Payment payment;

    @BeforeEach
    void setUp() {
        ForeignKey userId = new ForeignKey(1L);
        ForeignKey reservationId = new ForeignKey(2L);
        Money amount = new Money(1000L);
        PaymentType paymentType = PaymentType.WALLET;
        PaymentStatus paymentStatus = PaymentStatus.PENDING;

        payment = new Payment(userId, reservationId, amount, paymentType, paymentStatus);
    }

    @Test
    @DisplayName("cancel() - change status to CANCEL")
    void should_changePaymentStatusToCancel_when_cancel() {
        // when
        payment.cancel();

        // then
        assertEquals(PaymentStatus.CANCEL, payment.getPaymentStatus(), "Payment status should be 'CANCEL'");
    }
}