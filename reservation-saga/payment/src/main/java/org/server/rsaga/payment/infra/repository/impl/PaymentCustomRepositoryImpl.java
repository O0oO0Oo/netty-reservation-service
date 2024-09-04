package org.server.rsaga.payment.infra.repository.impl;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;
import org.server.rsaga.payment.domain.Payment;
import org.server.rsaga.payment.infra.repository.PaymentCustomRepository;
import org.server.rsaga.payment.infra.repository.PaymentJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class PaymentCustomRepositoryImpl implements PaymentCustomRepository {
    private final PaymentJpaRepository paymentJpaRepository;
    private final EntityManager entityManager;

    @Override
    public Payment findByReservationIdAndUserId(ForeignKey reservationId, ForeignKey userId) {
        return paymentJpaRepository.findByReservationIdAndUserId(
                reservationId, userId
        ).orElseThrow(
                () -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND)
        );
    }

    @Override
    public void batchSave(Collection<Payment> payments) {
        for (Payment payment : payments) {
            entityManager.persist(payment);
        }
        entityManager.flush();
    }
}