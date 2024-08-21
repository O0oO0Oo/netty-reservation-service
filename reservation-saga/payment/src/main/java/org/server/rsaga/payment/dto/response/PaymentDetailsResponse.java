package org.server.rsaga.payment.dto.response;

import org.server.rsaga.payment.domain.Payment;
import org.server.rsaga.payment.domain.constant.PaymentStatus;
import org.server.rsaga.common.domain.constant.PaymentType;

import java.util.ArrayList;
import java.util.List;

public record PaymentDetailsResponse(
        long paymentId,
        long userId,
        long reservationId,
        PaymentType paymentType,
        PaymentStatus paymentStatus
) {
    public static List<PaymentDetailsResponse> of(List<Payment> payments) {
        List<PaymentDetailsResponse> responses = new ArrayList<>(payments.size() + 1);

        for (Payment payment : payments) {
            responses.add(
                    new PaymentDetailsResponse(
                            payment.getId(),
                            payment.getUserId(),
                            payment.getReservationId(),
                            payment.getPaymentType(),
                            payment.getPaymentStatus()
                    )
            );
        }

        return responses;
    }
}
