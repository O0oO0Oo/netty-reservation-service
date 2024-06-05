package com.server.reservation.reservableitem.repository;

import com.server.reservation.reservableitem.entity.ReservableItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservableItemRepository extends JpaRepository<ReservableItem, Long> {
}
