package com.server.reservation.reservation.repository;

import com.server.reservation.reservation.domain.Reservation;
import com.server.reservation.reservation.domain.ReservationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);

    Optional<Reservation> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"reservableItem", "user"})
    Optional<Reservation> findWithReservableItemAndUserByIdAndUserId(Long id, Long userId);

    @Query("SELECT SUM(r.quantity) " +
            "FROM Reservation r " +
            "WHERE r.reservableItem.id = :itemId AND r.user.id = :userId AND r.reservationStatus = :status")
    Integer findSumQuantityByUserIdAndReservableItemIdAndReserved(
            @Param("itemId")
            Long itemId,
            @Param("userId")
            Long userId,
            @Param("status")
            ReservationStatus status);
}
