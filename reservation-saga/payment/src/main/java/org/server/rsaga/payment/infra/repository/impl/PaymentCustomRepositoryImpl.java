package org.server.rsaga.payment.infra.repository.impl;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;
import org.server.rsaga.payment.domain.Payment;
import org.server.rsaga.payment.infra.repository.PaymentCustomRepository;
import org.server.rsaga.payment.infra.repository.PaymentJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentCustomRepositoryImpl implements PaymentCustomRepository {
    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment findByReservationIdAndUserId(ForeignKey reservationId, ForeignKey userId) {
        return paymentJpaRepository.findByReservationIdAndUserId(
                reservationId, userId
        ).orElseThrow(
                () -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND)
        );
    }
}