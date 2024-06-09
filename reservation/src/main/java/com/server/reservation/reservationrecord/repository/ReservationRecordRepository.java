package com.server.reservation.reservationrecord.repository;

import com.server.reservation.reservationrecord.entity.ReservationRecord;
import com.server.reservation.reservationrecord.entity.ReservationRecordStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRecordRepository extends JpaRepository<ReservationRecord, Long> {
    List<ReservationRecord> findByUserId(Long userId);

    Optional<ReservationRecord> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"reservableItem", "user"})
    Optional<ReservationRecord> findWithReservableItemAndUserByIdAndUserId(Long id, Long userId);

    @Query("SELECT SUM(r.quantity) " +
            "FROM ReservationRecord r " +
            "WHERE r.reservableItem.id = :itemId AND r.user.id = :userId AND r.reservationRecordStatus = :status")
    Integer findSumQuantityByUserIdAndReservableItemIdAndReserved(
            @Param("itemId")
            Long itemId,
            @Param("userId")
            Long userId,
            @Param("status")
            ReservationRecordStatus status);
}
