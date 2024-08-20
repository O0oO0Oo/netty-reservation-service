package org.server.rsaga.reservation.infra.repository.impl;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;
import org.server.rsaga.reservation.domain.Reservation;
import org.server.rsaga.reservation.infra.repository.ReservationCustomRepository;
import org.server.rsaga.reservation.infra.repository.ReservationJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReserevationCustomRepositoryImpl implements ReservationCustomRepository {
    private final ReservationJpaRepository reservationJpaRepository;

    @Override
    public Reservation findReservationByIdOrElseThrow(long reservationId) {
        return reservationJpaRepository.findById(reservationId)
                .orElseThrow(
                        () -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND)
                );
    }

    @Override
    public Reservation findReservationByIdAndUserIdOrElseThrow(long reservationId, long userId) {
        return reservationJpaRepository.findByIdAndUserId(reservationId, userId)
                .orElseThrow(
                        () -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND)
                );
    }
}
