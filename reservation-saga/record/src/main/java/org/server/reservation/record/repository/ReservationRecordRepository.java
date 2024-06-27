package org.server.reservation.record.repository;

import org.server.reservation.record.entity.ReservationRecord;
import org.server.reservation.record.entity.ReservationRecordStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRecordRepository extends JpaRepository<ReservationRecord, Long> {
    List<ReservationRecord> findByUserId(Long userId);

    Optional<ReservationRecord> findByIdAndUserId(Long id, Long userId);

    // TODO : 엔티티 그래프 불가능
//    @EntityGraph(attributePaths = {"reservableItem", "user"})
//    Optional<ReservationRecord> findWithReservableItemAndUserByIdAndUserId(Long id, Long userId);

    @Query("SELECT SUM(r.quantity) " +
            "FROM ReservationRecord r " +
            "WHERE r.itemId = :itemId AND r.userId = :userId AND r.reservationRecordStatus = :status")
    Integer findSumQuantityByUserIdAndReservableItemIdAndReserved(
            @Param("itemId")
            Long itemId,
            @Param("userId")
            Long userId,
            @Param("status")
            ReservationRecordStatus status);
}