package org.server.rsaga.reservableitem.infra.repository;

import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.reservableitem.domain.ReservableItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservableItemJpaRepository extends JpaRepository<ReservableItem, Long> {

    @Query("SELECT r FROM ReservableItem r JOIN FETCH r.reservableTimes WHERE r.businessId = :businessId")
    List<ReservableItem> findByBusinessIdWithTimes(@Param("businessId") ForeignKey businessId);
    @EntityGraph(attributePaths = {"reservableTimes"})
    Optional<ReservableItem> findByIdAndBusinessId(Long id, ForeignKey businessId);

    // todo 쿼리 개선
    @Query("SELECT ri FROM ReservableItem ri " +
            "JOIN FETCH ri.reservableTimes rt " +
            "WHERE ri.id = :itemId " +
            "AND ri.businessId = :businessId " +
            "AND rt.id = :timeId")
    Optional<ReservableItem> findReservableItemsWithTimes(
            @Param("itemId") Long itemId,
            @Param("businessId") ForeignKey businessId,
            @Param("timeId") Long timeId);

    @Query("SELECT r FROM ReservableItem r JOIN FETCH r.reservableTimes t WHERE r.id = :reservableItemId AND t.id = :timeId")
    Optional<ReservableItem> findByIdAndReservableTimeId(@Param("reservableItemId") Long reservableItemId, @Param("timeId") Long timeId);
}