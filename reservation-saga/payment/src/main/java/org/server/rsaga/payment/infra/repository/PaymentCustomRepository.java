package org.server.rsaga.payment.infra.repository;

import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.payment.domain.Payment;

import java.util.Collection;

public interface PaymentCustomRepository {
    Payment findByReservationIdAndUserId(ForeignKey reservationId, ForeignKey userId);
    void batchSave(Collection<Payment> payments);
}
