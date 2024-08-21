package org.server.rsaga.reservation.infra.repository;

import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.reservation.domain.Reservation;

public interface ReservationCustomRepository {
    Reservation findReservationByIdOrElseThrow(long reservationId);
    Reservation findReservationByIdAndUserIdOrElseThrow(long reservationId, ForeignKey userId);
}
