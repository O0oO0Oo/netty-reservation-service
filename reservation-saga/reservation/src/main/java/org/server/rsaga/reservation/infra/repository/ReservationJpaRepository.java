package org.server.rsaga.reservation.infra.repository;

import org.server.rsaga.reservation.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);

    Optional<Reservation> findByIdAndUserId(Long id, Long userId);

    // TODO : 엔티티 그래프 불가능
//    @EntityGraph(attributePaths = {"reservableItem", "user"})
//    Optional<ReservationRecord> findWithReservableItemAndUserByIdAndUserId(Long id, Long userId);

    @Query("SELECT COALESCE(SUM(r.quantity), 0) " +
            "FROM Reservation r " +
            "WHERE r.userId = :userId AND r.reservableItemId = :reservableItemId " +
            "AND r.reservationStatus IN (" +
            "org.server.rsaga.reservation.domain.constant.ReservationStatus.RESERVED, " +
            "org.server.rsaga.reservation.domain.constant.ReservationStatus.COMPLETED, " +
            "org.server.rsaga.reservation.domain.constant.ReservationStatus.PENDING)")
    Integer findSumQuantityByUserIdAndReservableItemIdAndReserved(
            @Param("userId") Long userId,
            @Param("reservableItemId") Long reservableItemId);
}