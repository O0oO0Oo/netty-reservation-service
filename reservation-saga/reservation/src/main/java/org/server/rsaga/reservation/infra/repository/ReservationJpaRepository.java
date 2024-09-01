package org.server.rsaga.reservation.infra.repository;

import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.reservation.domain.Reservation;
import org.server.rsaga.reservation.dto.repository.UserItemPairDto;
import org.server.rsaga.reservation.dto.repository.UserItemReservationSumProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(ForeignKey userId);

    Optional<Reservation> findByIdAndUserId(Long id, ForeignKey userId);

    @Query("SELECT COALESCE(SUM(r.quantity), 0) " +
            "FROM Reservation r " +
            "WHERE r.userId = :userId AND r.reservableItemId = :reservableItemId " +
            "AND r.reservationStatus IN (" +
            "org.server.rsaga.reservation.domain.constant.ReservationStatus.RESERVED, " +
            "org.server.rsaga.reservation.domain.constant.ReservationStatus.COMPLETED, " +
            "org.server.rsaga.reservation.domain.constant.ReservationStatus.PENDING)")
    Integer findSumQuantityByUserIdAndReservableItemIdAndReserved(
            @Param("userId") ForeignKey userId,
            @Param("reservableItemId") ForeignKey reservableItemId);
}