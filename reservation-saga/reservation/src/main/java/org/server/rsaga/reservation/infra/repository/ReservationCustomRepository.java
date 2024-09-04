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

    /**
     * TSID 를 pk 로 사용하기 때문에, {@link ReservationJpaRepository} 의 saveAll() 을 사용하면 각 id 검증을 위해 Select 쿼리가 방생한다.
     * @param reservations
     */
    void batchSave(List<Reservation> reservations);
}
