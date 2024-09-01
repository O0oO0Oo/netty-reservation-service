package org.server.rsaga.reservation.infra.repository;

import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.reservation.domain.Reservation;
import org.server.rsaga.reservation.dto.repository.UserItemPairDto;
import org.server.rsaga.reservation.dto.repository.UserItemReservationSumProjection;

import java.util.List;
import java.util.Set;

public interface ReservationCustomRepository {
    Reservation findReservationByIdOrElseThrow(long reservationId);
    Reservation findReservationByIdAndUserIdOrElseThrow(long reservationId, ForeignKey userId);
    List<UserItemReservationSumProjection> findSumQuantityByUserIdAndReservableItemIdIn(Set<UserItemPairDto> userItemPairs);
}
