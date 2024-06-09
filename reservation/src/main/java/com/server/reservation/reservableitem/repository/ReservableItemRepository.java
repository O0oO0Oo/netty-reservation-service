package com.server.reservation.reservableitem.repository;

import com.server.reservation.reservableitem.entity.ReservableItem;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface ReservableItemRepository extends JpaRepository<ReservableItem, Long> {
    List<ReservableItem> findByBusinessId(Long businessId);

    Optional<ReservableItem> findByIdAndBusinessId(Long id, Long businessId);
}
