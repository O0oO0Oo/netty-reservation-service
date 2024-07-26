package org.server.rsaga.reservation.repository;

import org.server.rsaga.reservation.entity.Reservation;
import org.server.rsaga.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRecordRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);

    Optional<Reservation> findByIdAndUserId(Long id, Long userId);

    // TODO : 엔티티 그래프 불가능
//    @EntityGraph(attributePaths = {"reservableItem", "user"})
//    Optional<ReservationRecord> findWithReservableItemAndUserByIdAndUserId(Long id, Long userId);

    @Query("SELECT SUM(r.quantity) " +
            "FROM Reservation r " +
            "WHERE r.itemId = :itemId AND r.userId = :userId AND r.reservationStatus = :status")
    Integer findSumQuantityByUserIdAndReservableItemIdAndReserved(
            @Param("itemId")
            Long itemId,
            @Param("userId")
            Long userId,
            @Param("status")
            ReservationStatus status);
}