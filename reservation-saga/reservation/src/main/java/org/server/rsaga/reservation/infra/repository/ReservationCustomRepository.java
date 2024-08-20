package org.server.rsaga.reservation.infra.repository;

import org.server.rsaga.reservation.domain.Reservation;

public interface ReservationCustomRepository {
    Reservation findReservationByIdOrElseThrow(long reservationId);
    Reservation findReservationByIdAndUserIdOrElseThrow(long reservationId, long userId);
}
